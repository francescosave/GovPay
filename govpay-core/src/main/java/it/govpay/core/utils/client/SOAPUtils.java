/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2017 Link.it srl (http://www.link.it).
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govpay.core.utils.client;

import gov.telematici.pagamenti.ws.ppthead.IntestazioneCarrelloPPT;
import it.govpay.core.utils.JaxbUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.Schema;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

public class SOAPUtils {
	
	private static XMLInputFactory xif = XMLInputFactory.newInstance();

	public static void writeMessage(JAXBElement<?> body, Object header, OutputStream baos) throws JAXBException, SAXException, IOException {
		baos.write("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">".getBytes());
		if(header != null) {
			baos.write("<soap:Header>".getBytes());
			if(header instanceof IntestazioneCarrelloPPT) {
				JaxbUtils.marshalIntestazioneCarrelloPPT((IntestazioneCarrelloPPT)header, baos);
			} else {
				JaxbUtils.marshal(header, baos);
			}
			baos.write("</soap:Header>".getBytes());
		}
		baos.write("<soap:Body>".getBytes());
		JaxbUtils.marshal(body, baos);
		baos.write("</soap:Body>".getBytes());
		baos.write("</soap:Envelope>".getBytes());
	}
	
	public static Object unmarshal(InputStream is, Schema schema) throws JAXBException, SAXException, IOException, XMLStreamException {
		
        XMLStreamReader xsr = xif.createXMLStreamReader(is);
        
        boolean isBody = false;
        while(!isBody) {
        	xsr.nextTag();
        	if(xsr.isStartElement()) {
        		String local = xsr.getLocalName();
        		isBody = local.equals("Body");
        	}
        }
        
        xsr.nextTag();
        if(!xsr.isStartElement()) {
        	// Body vuoto
        	return null;
        } else {
        	return JaxbUtils.unmarshal(xsr, schema);
        }
	}
	
	public static JAXBElement<?> toJaxb(byte[] msg, Schema schema) throws JAXBException, SAXException, IOException, XMLStreamException {
		String s = new String(msg);
		InputStream is = IOUtils.toInputStream(s);
		return  (JAXBElement<?>) unmarshal(is, schema);
	}
	
	public static Object unmarshal(byte[] msg, Schema schema) throws JAXBException, SAXException, IOException, XMLStreamException {
		String s = new String(msg);
		InputStream is = IOUtils.toInputStream(s);
		return  unmarshal(is, schema);
	}
	
	
}
