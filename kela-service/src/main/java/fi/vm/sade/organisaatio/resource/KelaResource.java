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
package fi.vm.sade.organisaatio.resource;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import fi.vm.sade.rajapinnat.kela.KelaGenerator;

@Path("/kela")
@Component
@Api(value = "/kela", description = "Kelan operaatiot")
public class KelaResource {
	 
    @Autowired
    @Qualifier("kelaTask")
	private TaskExecutor taskExecutor;

	public enum Command {
    	START,
    	STOP,
    	STATUS,
    	LOG
    }

    public enum Response
    {
    	UNKNOWN_COMMAND,
    	ALREADY_RUNNING,
    	FAILED,
    	STARTED,
    	STOP_REQUESTED,
    	STATUS,
    	NOT_STARTED,
    	FILE_NOT_FOUND
    }

    private KelaGenerator kelaGenerator;
    private Thread kelaGeneratorThread;
    
    @GET
    @Path("/export")
    @Produces("text/plain")
    @ApiOperation(value = "Vie Kelan tiedot", notes = "Operaatio vie Kelan tiedot.", response = String.class)
    public synchronized String exportKela(@QueryParam("command") final String command,
    		@QueryParam("options") final String options
    		) {
    	Command cmd;
    	try {
    		cmd = Command.valueOf(command);
    	} catch (IllegalArgumentException e) {
    		return Response.UNKNOWN_COMMAND.name()+": "+command;
    	}
    	switch (cmd) {
    		case START:  return start(options);
    		case STOP:  return stop(); 
    		case STATUS: return status();
    		case LOG: return log();
    	}
    	return null; //should never be reached
    }
    
    private String start(String options) {
    	if (kelaGenerator!=null && !KelaGenerator.startableStates.contains(kelaGenerator.getThreadState().getRunState())) {
    		return Response.ALREADY_RUNNING.name();
    	}
    	try {
    		final ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/context/bundle-context.xml");
    		kelaGenerator = context.getBean(KelaGenerator.class);
    		kelaGeneratorThread = new Thread((Runnable) kelaGenerator);
    		if (!StringUtils.isEmpty(options)) {
    			kelaGenerator.setOptions(options);
    		}
    		taskExecutor.execute(kelaGeneratorThread);
    	}catch(Exception e) {
    		e.printStackTrace();
    		kelaGenerator=null;
    		return Response.FAILED.name();
    	}
        return Response.STARTED.name();
    }
    
    private String stop() {
    	if (kelaGenerator==null) {
    		return Response.NOT_STARTED.name();
    	}
    	kelaGenerator.stop();
        return Response.STOP_REQUESTED.name();
    }
    
    private String status() {
    	if (null == kelaGenerator) {
    		return Response.NOT_STARTED.name();
    	}
    	return Response.STATUS.name()+": "+kelaGenerator.getThreadState();
    }

    private String log() {
    	if (null == kelaGenerator) {
    		return Response.NOT_STARTED.name();
    	}
    	return readFile(kelaGenerator.getThreadState().getLogFileName());
    }

    private String readFile( String file ) {
        BufferedReader reader;
		try {
			reader = new BufferedReader( new FileReader (file));
		} catch (FileNotFoundException e1) {
			return Response.FILE_NOT_FOUND.name()+": "+file;
		}

		String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        try {
			while( ( line = reader.readLine() ) != null ) {
			    stringBuilder.append( line );
			    stringBuilder.append( ls );
			}
		} catch (IOException e) {
			return e.getMessage();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        return stringBuilder.toString();
    }
}
