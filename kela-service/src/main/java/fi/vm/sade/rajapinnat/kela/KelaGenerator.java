/*
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.rajapinnat.kela;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Configurable
public class KelaGenerator implements Runnable {

    private static final Logger LOG = Logger.getLogger(KelaGenerator.class);

    @Autowired
    private WriteOPTILI optiliWriter;
    @Autowired
    private WriteOPTINI optiniWriter;
    @Autowired
    private WriteOPTIOL optiolWriter;
    @Autowired
    private WriteOPTITU optituWriter;
    @Autowired
    private WriteOPTIYH optiyhWriter;
    @Autowired
    private WriteOPTIYT optiytWriter;
    @Autowired
    private WriteOPTIOR optiorWriter;
    @Autowired
    private WriteLOG optiLog;  // NOTE: the only purpose of this class is to neatly get LOG file name 

    Map<String, AbstractOPTIWriter> allOptiWriters = new HashMap<String, AbstractOPTIWriter>();
    private List<AbstractOPTIWriter> selectedOptiWriters = new LinkedList<AbstractOPTIWriter>();

    @Autowired
    private OrganisaatioContainer orgContainer;

    @Autowired
    ProducerTemplate producerTemplate;

    @Value("${socksProxy:off}")
    public void setSocksProxy(String socksProxyOn) {
        if (socksProxyOn.equalsIgnoreCase("ON")) {
            setSocksProxyOn();
        }
    }

    long startTime = 0;
    long endTime = 0;

    private String protocol;

    private String host;
    private String username;
    private String password = "<not set>";
    private String sourcePath;
    private String targetPath;
    private String dataTimeout;

    private boolean send = true;
    private boolean generate = true;
    private boolean init = true;

    public void setSendonly() {
        generate = false;
        init = false;
    }

    public void setGenerateonly() {
        send = false;
    }

    public void setInitonly() {
        this.send = false;
        this.generate = false;
    }
    
    public void clearOptions() {
        send = true;
        generate = true;
        init = true;
    }

    private void initReports() throws UserStopRequestException {
        long startTime = System.currentTimeMillis();
        orgContainer.fetchOrganisaatiot();
        LOG.info("Fetch time: " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
    }

    /**
     * Generates all KELA-OPTI transfer files currently implemented
     *
     * @throws UserStopRequestException
     */
    public void generateKelaFiles() throws UserStopRequestException, Exception {
        long startTime = System.currentTimeMillis();
        for (AbstractOPTIWriter optiWriter : selectedOptiWriters) {
            writeKelaFile(optiWriter);
        }
        LOG.info("All files generated");
        LOG.info("Generation time: " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
    }

    private String mkTargetUrl(String protocol, String username,
            String host, String targetPath, String password,
            String dataTimeout) {
        return String.format("%s%s%s%s%s%s%s%s%s%s",
                protocol,
                "://",
                username,
                "@",
                host,
                targetPath,
                "?password=",
                password,
                "&ftpClient.dataTimeout=",
                dataTimeout + "&passiveMode=true");
    }

    private String targetUrl;

    /**
     * Performs ftp transfer of generated kela-opti files.
     *
     * @throws Exception
     */
    public void transferFiles() throws Exception {
        LOG.info("transferFiles: target url: " + mkTargetUrl(protocol, username, host, targetPath, "???", dataTimeout));
        targetUrl = mkTargetUrl(protocol, username, host, targetPath, password, dataTimeout);
        for (AbstractOPTIWriter optiWriter : selectedOptiWriters) {
            sendFile(optiWriter);
        }
        LOG.info("Files transferred");
    }

    private void sendFile(AbstractOPTIWriter writer) throws UserStopRequestException {
        if (runState.equals(RunState.STOP_REQUESTED)) {
            throw new UserStopRequestException();
        }
        inTurn = writer;
        LOG.info("Sending: " + writer.getFileName() + "...");
        producerTemplate.sendBodyAndHeader(targetUrl, new File(writer.getFileName()), Exchange.FILE_NAME, writer.getFileLocalName());
        LOG.info("Done.");
    }

    private AbstractOPTIWriter inTurn;

    private void writeKelaFile(AbstractOPTIWriter kelaWriter) throws UserStopRequestException, Exception {
        try {
            inTurn = kelaWriter;
            long time = System.currentTimeMillis();
            LOG.info(String.format("Generating file %s...", kelaWriter.getFileName()));
            kelaWriter.writeStream();
            LOG.info("Generation time: " + (System.currentTimeMillis() - time) / 1000.0 + " seconds");
        } catch (UserStopRequestException e) {
            throw e;
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            throw ex;
        }
    }

    @Value("${transferprotocol}")
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Value("${transferhost}")
    public void setHost(String host) {
        this.host = host;
    }

    @Value("${transferuser}")
    public void setUsername(String username) {
        this.username = username;
    }

    @Value("${transferpassword}")
    public void setPassword(String password) {
        this.password = password;
    }

    @Value("${exportdir}")
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    @Value("${targetPath}")
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    @Value("${dataTimeout}")
    public void setDataTimeout(String dataTimeout) {
        this.dataTimeout = dataTimeout;
    }

    public String getDataTimeout() {
        return dataTimeout;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public static void setSocksProxyOn() {
        Properties props = System.getProperties();
        props.put("socksProxyHost", "127.0.0.1");
        LOG.info("socksProxyHost: 127.0.0.1");
        props.put("socksProxyPort", "9090");
        LOG.info("socksProxyPort: 9090");
        System.setProperties(props);
    }

    public void stop() {
        if (!runState.equals(RunState.RUNNING) && !runState.equals(RunState.TRANSFER)) {
            LOG.warn("Generation interrupt-request received - but generation is not running.");
            return;
        }
        runState = RunState.STOP_REQUESTED;
        LOG.warn("Generation interrupt-request received.");
        if (orgContainer != null) {
            orgContainer.stop();
        }
        if (inTurn != null) {
            inTurn.stop();
        }
    }

    public enum RunState {
        IDLE,
        LOG_FILE_ERROR,
        RUNNING,
        STOP_REQUESTED,
        STOPPED,
        ERROR,
        TRANSFER,
        DONE
    }

    public enum StaticOptions {
        INITONLY,
        SENDONLY,
        GENERATEONLY
    }

    public static final List<RunState> startableStates = Arrays.asList(RunState.IDLE, RunState.LOG_FILE_ERROR, RunState.ERROR, RunState.STOPPED, RunState.DONE);

    public static final String LOGGERNAME = "KelaGeneratorLogger";

    private FileAppender fa = new FileAppender();

    {
        fa.setName(LOGGERNAME);
        fa.setLayout(new PatternLayout("%d{ABSOLUTE} %5p - %m%n"));
        fa.setThreshold(Level.INFO);
        fa.setAppend(true);
    }

    private boolean canWriteFile(String fileName) {
        File f = new File(fileName);

        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }
        return f.canWrite();
    }

    private void initLogger() {
        if (!canWriteFile(optiLog.getFileName())) {
            runState = RunState.LOG_FILE_ERROR;
            return;
        }

        fa.setFile(optiLog.getFileName());
        LOG.addAppender(fa);
        fa.activateOptions();
    }

    @PostConstruct
    private void initWriters() {
        allOptiWriters.put(optiliWriter.getFileIdentifier().toUpperCase(), optiliWriter);
        allOptiWriters.put(optiniWriter.getFileIdentifier().toUpperCase(), optiniWriter);
        allOptiWriters.put(optiolWriter.getFileIdentifier().toUpperCase(), optiolWriter);
        allOptiWriters.put(optituWriter.getFileIdentifier().toUpperCase(), optituWriter);
        allOptiWriters.put(optiyhWriter.getFileIdentifier().toUpperCase(), optiyhWriter);
        allOptiWriters.put(optiytWriter.getFileIdentifier().toUpperCase(), optiytWriter);
        allOptiWriters.put(optiorWriter.getFileIdentifier().toUpperCase(), optiorWriter);
        selectedOptiWriters.addAll(allOptiWriters.values()); //default
    }

    public void setOptions(String opts) throws Exception {
        if (!KelaGenerator.startableStates.contains(runState)) {
            LOG.error("options may only be set when process is not running or run.");
            throw new Exception("option error");
        }

        selectedOptiWriters = new LinkedList<AbstractOPTIWriter>();
        String[] options = opts.split(",");
        for (String optionUnTrimmed : options) {
            String option_s = optionUnTrimmed.trim().toUpperCase();
            if (allOptiWriters.containsKey(option_s)) {
                selectedOptiWriters.add(allOptiWriters.get(option_s));
                continue;
            }

            StaticOptions option;
            try {
                option = StaticOptions.valueOf(option_s);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Unknown option: " + option_s);
            }

            switch (option) {
                case GENERATEONLY:
                    setGenerateonly();
                    break;
                case SENDONLY:
                    setSendonly();
                    break;
                case INITONLY:
                    setInitonly();
                    break;
                default:
                    throw new RuntimeException("Unknown option error: option=" + option);
            }
        }

        if (selectedOptiWriters.size() == 0) {
            selectedOptiWriters.addAll(allOptiWriters.values()); //default
        }
    }

    private void releaseLogger() {
        LOG.removeAppender(fa);
    }

    private RunState runState = RunState.IDLE;

    @Override
    public void run() {
        if (!KelaGenerator.startableStates.contains(runState)) {
            return;
        }
        endTime = 0;
        Ticker ticker = new Ticker(this, tickerInterval);
        try {
            initLogger();
            Thread tickerThread = new Thread(ticker);
            startTime = System.currentTimeMillis();
            runState = RunState.RUNNING;
            tickerThread.start();

            if (init) {
                initReports();
            }
            if (generate) {
                this.generateKelaFiles();
            } else {
                LOG.info("Skipped generate files");
            }
            if (send) {
                runState = RunState.TRANSFER;
                this.transferFiles();
            } else {
                LOG.info("Skipped transfer files");
            }
            runState = RunState.DONE;
        } catch (UserStopRequestException e) {
            runState = RunState.STOPPED;
            LOG.error("Interrupted.");
        } catch (Exception ex) {
            runState = RunState.ERROR;
            LOG.error(ex.getMessage());
            ex.printStackTrace();
        } finally {
            releaseLogger();
            ticker.stop();
            endTime = System.currentTimeMillis();
            LOG.info("duration: " + (long) ((endTime - startTime) / 1000.0) + "s.");
        }
    }

    public static class KelaGeneratorProgress {

        RunState runState;

        public KelaGeneratorProgress(RunState runState, String phase,
                String logFileName, long time) {
            super();
            this.runState = runState;
            this.phase = phase;
            this.logFileName = logFileName;
            this.time = time;
        }
        String phase;
        String logFileName;
        long time;

        public RunState getRunState() {
            return runState;
        }

        public String getPhase() {
            return phase;
        }

        public String getLogFileName() {
            return logFileName;
        }

        public long getTime() {
            return time;
        }

        @Override
        public String toString() {
            return "process:[" + runState + "] phase:[" + phase + "] time:[" + time + "s] logfile:[" + logFileName + "]";
        }
    }

    private int tickerInterval = 0;

    @Value("${ticker.interval:0}")
    public void setTickerInterval(String tickerInterval) {
        this.tickerInterval = Integer.parseInt(tickerInterval);
    }

    public static class Ticker implements Runnable {

        KelaGenerator kg;
        boolean stop = false;
        int tickerInterval = 0;

        public void stop() {
            stop = true;
        }

        public Ticker(KelaGenerator kg, int tickerInterval) {
            super();
            this.kg = kg;
            this.tickerInterval = tickerInterval;
        }

        @Override
        public void run() {
            if (tickerInterval > 0) {
                LOG.info("(TICKER) interval: " + tickerInterval + "s");
                try {
                    while (!stop) {
                        Thread.sleep(tickerInterval * 1000);
                        LOG.info("(TICKER) " + kg.getThreadState());
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public KelaGeneratorProgress getThreadState() {
        return new KelaGeneratorProgress(
                runState,
                (runState.equals(RunState.RUNNING) ? (inTurn == null ? "INIT" : inTurn.getFileIdentifier()) : "NONE"),
                optiLog.getFileName(),
                (endTime > 0 ? (long) ((endTime - startTime) / 1000.0) : (long) ((System.currentTimeMillis() - startTime) / 1000.0)));
    }

    static void error(String format, Object... args) {
        LOG.error(String.format(format, args));
    }

    static void warn(String format, Object... args) {
        LOG.warn(String.format(format, args));
    }

    static void info(String format, Object... args) {
        LOG.info(String.format(format, args));
    }

    static void debug(String format, Object... args) {
        LOG.debug(String.format(format, args));
    }
}
