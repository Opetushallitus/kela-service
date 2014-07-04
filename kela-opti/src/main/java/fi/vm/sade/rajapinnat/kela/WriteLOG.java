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

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Configurable
/* NOTE: the only purpouse of this class is to neatly get LOG file name */

public class WriteLOG extends AbstractOPTIWriter {
	@Autowired
	private ApplicationContext appContext;
	@Override
	public void composeRecords() throws IOException, UserStopRequestException {}

	@Override
	public String composeRecord(Object... args) throws OPTFormatException {
		return null;
	}
	@Override
	public String getAlkutietue() {
		return null;
	}
	@Override
	public String getLopputietue() {
		return null;
	}
	@Override
	public String getFileIdentifier() {
		return "LOG";
	}
}
