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
package it.govpay.web.rs.dars.manutenzione.strumenti;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import it.govpay.bd.BasicBD;
import it.govpay.web.rs.dars.base.DarsHandler;
import it.govpay.web.rs.dars.base.DarsService;
import it.govpay.web.rs.dars.exception.ConsoleException;
import it.govpay.web.rs.dars.exception.DuplicatedEntryException;
import it.govpay.web.rs.dars.exception.ExportException;
import it.govpay.web.rs.dars.exception.ValidationException;
import it.govpay.web.rs.dars.handler.IDarsHandler;
import it.govpay.web.rs.dars.model.Dettaglio;
import it.govpay.web.rs.dars.model.Elemento;
import it.govpay.web.rs.dars.model.Elenco;
import it.govpay.web.rs.dars.model.InfoForm;
import it.govpay.web.rs.dars.model.RawParamValue;
import it.govpay.web.rs.dars.model.Voce;
import it.govpay.web.utils.ConsoleProperties;
import it.govpay.web.utils.Utils;

public class StrumentiHandler extends DarsHandler<Object> implements IDarsHandler<Object> {

	public StrumentiHandler(Logger log, DarsService darsService) {
		super(log, darsService);
	}

	@Override
	public Elenco getElenco(UriInfo uriInfo, BasicBD bd) throws WebApplicationException, ConsoleException {
		String methodName =  this.titoloServizio + ": getElencoOperazioni";
		try{
			this.log.info("Esecuzione " + methodName + " in corso..."); 
			// Operazione consentita solo agli utenti che hanno almeno un ruolo consentito per la funzionalita'
			this.darsService.checkDirittiServizio(bd, this.funzionalita);
			long count = 0;

			Elenco elenco = new Elenco(this.titoloServizio, 
					this.getInfoRicerca(uriInfo, bd),	this.getInfoCreazione(uriInfo, bd), count, this.getInfoEsportazione(uriInfo, bd), this.getInfoCancellazione(uriInfo, bd)); 

			String[] listaOperazioni =  ConsoleProperties.getInstance().getOperazioniJMXDisponibili(); 

			for (int i = 0; i < listaOperazioni.length; i++) {
				String operazione = listaOperazioni[i];
				long idOperazione = i;
				String titoloOperazione = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + "."+operazione+".titolo");
				String sottotitoloOperazione = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + "."+operazione+".sottotitolo");
				URI urlDettaglio = Utils.creaUriConPath(this.pathServizio, idOperazione+"");
				Elemento elemento = new Elemento(idOperazione, titoloOperazione, sottotitoloOperazione, urlDettaglio);
				elenco.getElenco().add(elemento);
			}

			this.log.info("Esecuzione " + methodName + " completata.");

			return elenco;
		}catch(WebApplicationException e){
			throw e;
		}catch(Exception e){
			throw new ConsoleException(e);
		}
	}

	@Override
	public Dettaglio getDettaglio(long id, UriInfo uriInfo, BasicBD bd)
			throws WebApplicationException, ConsoleException {
		String methodName = this.titoloServizio + "Esecuzione dell'operazione con id: "+ id;

		try{
			String[] listaOperazioni =  ConsoleProperties.getInstance().getOperazioniJMXDisponibili(); 
			String operazione = listaOperazioni[(int) id];

			String titoloOperazione = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + "." + operazione + ".titolo");
			methodName = this.titoloServizio + "Esecuzione dell'operazione con id: "+ id +", CodiceOperazione: [" + titoloOperazione +"]";
			String nomeMetodo = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + "." + operazione + ".nomeMetodo");

			this.log.info("Esecuzione " + methodName + " in corso...");
			// Operazione consentita solo agli utenti che hanno almeno un ruolo consentito per la funzionalita'
			this.darsService.checkDirittiServizio(bd, this.funzionalita);

			InfoForm infoModifica = null;
			InfoForm infoCancellazione = null;
			InfoForm infoEsportazione = null;

			Dettaglio dettaglio = new Dettaglio(titoloOperazione, infoEsportazione, infoCancellazione, infoModifica);
			it.govpay.web.rs.dars.model.Sezione root = dettaglio.getSezioneRoot();

			Object invoke = null;
			log.debug("Invocazione operazione ["+nomeMetodo +"] in corso..."); 

			/*
			 * Se l'operazione e' un reset cache lo eseguo su tutti i nodi
			 * Altrimenti mi fermo al primo che ha successo
			 */
			Map<String, String> urlJMX = ConsoleProperties.getInstance().getUrlJMX();
			for(String nodo : urlJMX.keySet()) {

				String url = urlJMX.get(nodo);

				try{
					invoke = Utils.invocaOperazioneJMX(nomeMetodo, url, org.apache.log4j.Logger.getLogger(StrumentiHandler.class));

					if(id==3) {
						root.addVoce("Esito operazione sul nodo " + nodo, (String) invoke);
					} else {
						root.addVoce("Operazione completata sul nodo",nodo);

						popolaVociSezioneRoot(root, invoke);
					}

					if(id!=3) {
						break;
					}
				} catch(Exception e) {
					log.error("si e' verificato un errore durante l'esecuzione dell'operazione ["+nomeMetodo+"]: " + e.getMessage(),e); 
					Throwable t = e;
					while(t.getCause() != null) {
						t = t.getCause();
					}
					root.addVoce("Esito operazione sul nodo " + nodo, "Errore: " + t.getMessage());
				}
			}


			this.log.info("Esecuzione " + methodName + " completata.");

			return dettaglio;
		}catch(WebApplicationException e){
			throw e;
		}catch(Exception e){
			throw new ConsoleException(e);
		}
	}

	public void popolaVociSezioneRoot(it.govpay.web.rs.dars.model.Sezione root, Object invoke) {
		if(invoke != null && invoke instanceof String){
			String esito = (String) invoke;
			String[] voci = esito.split("\\|");

			for (String string : voci) {
				String[] voce = string.split("#");
				if(voce.length == 2)
					root.addVoce(voce[0],voce[1]);
				else
					if(voce.length == 1)
						root.addVoce(voce[0],null);
			}
		}
	}

	/* Operazioni non consentite */

	@Override
	public InfoForm getInfoRicerca(UriInfo uriInfo, BasicBD bd, boolean visualizzaRicerca, Map<String,String> parameters) throws ConsoleException {	
		URI ricerca = this.getUriRicerca(uriInfo, bd);
		InfoForm infoRicerca = new InfoForm(ricerca);
		return infoRicerca;
	}
	
	@Override
	public InfoForm getInfoCancellazioneDettaglio(UriInfo uriInfo, BasicBD bd, Object entry) throws ConsoleException {
		return null;
	}
	
	@Override
	public InfoForm getInfoEsportazione(UriInfo uriInfo, BasicBD bd, Map<String, String> parameters) throws ConsoleException { return null; }
	
	@Override
	public InfoForm getInfoEsportazioneDettaglio(UriInfo uriInfo, BasicBD bd, Object entry)	throws ConsoleException {	return null;	}
	
	@Override
	public InfoForm getInfoCancellazione(UriInfo uriInfo, BasicBD bd, Map<String, String> parameters) throws ConsoleException { return null;}

	@Override
	public InfoForm getInfoCreazione(UriInfo uriInfo, BasicBD bd) throws ConsoleException {	return null;	}

	@Override
	public InfoForm getInfoModifica(UriInfo uriInfo, BasicBD bd, Object entry) throws ConsoleException {	return null;	}

	@Override
	public Object getField(UriInfo uriInfo, List<RawParamValue> values, String fieldId, BasicBD bd)	throws WebApplicationException, ConsoleException {		return null;	}

	@Override
	public Object getSearchField(UriInfo uriInfo, List<RawParamValue> values, String fieldId, BasicBD bd)	throws WebApplicationException, ConsoleException { 	return null; }
	
	@Override
	public Object getDeleteField(UriInfo uriInfo, List<RawParamValue> values, String fieldId, BasicBD bd) throws WebApplicationException, ConsoleException { return null; }
	
	@Override
	public Object getExportField(UriInfo uriInfo, List<RawParamValue> values, String fieldId, BasicBD bd) throws WebApplicationException, ConsoleException { return null; }
	
	@Override
	public Elenco delete(List<Long> idsToDelete, List<RawParamValue> rawValues, UriInfo uriInfo, BasicBD bd)	throws WebApplicationException, ConsoleException {	return null;}

	@Override
	public Object creaEntry(InputStream is, UriInfo uriInfo, BasicBD bd) throws WebApplicationException, ConsoleException {	return null;}

	@Override
	public Dettaglio insert(InputStream is, UriInfo uriInfo, BasicBD bd)throws WebApplicationException, ConsoleException, ValidationException, DuplicatedEntryException {	return null;	}

	@Override
	public void checkEntry(Object entry, Object oldEntry) throws ValidationException {}

	@Override
	public Dettaglio update(InputStream is, UriInfo uriInfo, BasicBD bd) throws WebApplicationException, ConsoleException, ValidationException { return null; }

	@Override
	public String getTitolo(Object entry,BasicBD bd) {	return null; }

	@Override
	public String getSottotitolo(Object entry,BasicBD bd) { return null; }
	
	@Override
	public Map<String, Voce<String>> getVoci(Object entry, BasicBD bd) throws ConsoleException { return null; }

	@Override
	public String esporta(List<Long> idsToExport, List<RawParamValue> rawValues, UriInfo uriInfo, BasicBD bd, ZipOutputStream zout) throws WebApplicationException, ConsoleException,ExportException { return null; }

	@Override
	public String esporta(Long idToExport, List<RawParamValue> rawValues, UriInfo uriInfo, BasicBD bd, ZipOutputStream zout) throws WebApplicationException, ConsoleException,ExportException {	return null;	} 

	@Override
	public Object uplaod(MultipartFormDataInput input, UriInfo uriInfo, BasicBD bd)	throws WebApplicationException, ConsoleException, ValidationException { return null;}
}
