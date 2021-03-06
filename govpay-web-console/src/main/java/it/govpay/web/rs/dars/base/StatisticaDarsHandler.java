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
package it.govpay.web.rs.dars.base;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import it.govpay.bd.BasicBD;
import it.govpay.bd.reportistica.statistiche.filters.StatisticaFilter;
import it.govpay.model.reportistica.statistiche.TipoIntervallo;
import it.govpay.web.rs.dars.exception.ConsoleException;
import it.govpay.web.rs.dars.handler.IStatisticaDarsHandler;
import it.govpay.web.rs.dars.model.Voce;
import it.govpay.web.rs.dars.model.input.ParamField;
import it.govpay.web.rs.dars.model.input.base.InputDate;
import it.govpay.web.rs.dars.model.input.base.InputNumber;
import it.govpay.web.rs.dars.model.input.base.SelectList;
import it.govpay.web.rs.dars.model.statistiche.PaginaGrafico;
import it.govpay.web.utils.ConsoleProperties;
import it.govpay.web.utils.Utils;

public abstract class StatisticaDarsHandler<T> extends BaseDarsHandler<T> implements IStatisticaDarsHandler<T> {

	public StatisticaDarsHandler(Logger log, BaseDarsService darsService) {
		super(log, darsService);
	}

	@Override
	public abstract PaginaGrafico getGrafico(UriInfo uriInfo, BasicBD bd) throws WebApplicationException, ConsoleException;

	@Override
	public Map<String, ParamField<?>> getInfoGrafico(UriInfo uriInfo, BasicBD bd ) throws ConsoleException{
		return this.getInfoGrafico(uriInfo, bd, true);
	}

	@Override
	public Map<String, ParamField<?>> getInfoGrafico(UriInfo uriInfo, BasicBD bd, boolean visualizzaRicerca) throws ConsoleException{
		return this.getInfoGrafico(uriInfo, bd, visualizzaRicerca, null);
	}

	@Override
	public Map<String, ParamField<?>> getInfoGrafico(UriInfo uriInfo, BasicBD bd, Map<String, String> parameters)
			throws ConsoleException {
		return this.getInfoGrafico(uriInfo, bd, true, parameters);
	}

	protected Date calcolaAvanzamento(Date data, int avanzamento, TipoIntervallo tipoIntervallo) throws ConsoleException{
		if(avanzamento != 0){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(data);
			switch (tipoIntervallo) {
			case MENSILE:
				calendar.add(Calendar.MONTH, avanzamento);
				break;
			case GIORNALIERO:
				calendar.add(Calendar.DATE, avanzamento);
				break;
			case ORARIO:
				calendar.add(Calendar.HOUR, avanzamento);
				break;
			}

			return calendar.getTime();
		}

		return data;
	}

	protected StatisticaFilter popoloFiltroStatistiche(UriInfo uriInfo,BasicBD bd, StatisticaFilter filter) throws ConsoleException, Exception {
		Date data = new Date();
		String dataId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.data.id");
		String dataS = this.getParameter(uriInfo, dataId, String.class);
		if(StringUtils.isNotEmpty(dataS)){
			data = this.convertJsonStringToDate(dataS);
		}
		filter.setData(data);

		String colonneId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.colonne.id");
		String colonneS = this.getParameter(uriInfo, colonneId, String.class);
		int colonne = 0;
		if(StringUtils.isNotEmpty(colonneS)){
			try{
				colonne = Integer.parseInt(colonneS);
			}catch(Exception e){

			}
		}
		filter.setLimit(colonne);

		String tipoIntervalloId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo.id");
		String tipoIntervalloS = this.getParameter(uriInfo, tipoIntervalloId, String.class);
		TipoIntervallo tipoIntervallo= TipoIntervallo.GIORNALIERO;
		if(StringUtils.isNotEmpty(tipoIntervalloS)){
			try{
				tipoIntervallo= TipoIntervallo.valueOf(tipoIntervalloS);
			}catch(Exception e){
				tipoIntervallo = null;
			}
			
			if(tipoIntervallo == null)
				tipoIntervallo = this.toTipoIntervallo(tipoIntervalloS);
		}
		filter.setTipoIntervallo(tipoIntervallo);

		// soglia minima percentuale
		double soglia = ConsoleProperties.getInstance().getSogliaPercentualeMinimaGraficoTorta() / 100;
		filter.setSoglia(soglia);

		return filter;
	}

	protected void initInfoGrafico(UriInfo uriInfo, BasicBD bd) throws ConsoleException{
		if(this.infoRicercaMap == null)
			this.infoRicercaMap = new HashMap<String, ParamField<?>>();

		String dataId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.data.id");
		String colonneId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.colonne.id");
		String tipoIntervalloId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo.id");

		InputNumber colonne = new InputNumber(colonneId, null, null, true, true, false, 1, 20);
		this.infoRicercaMap.put(colonneId, colonne);

		InputDate data = new InputDate(dataId, null, new Date(), false, false, true, null, null);
		this.infoRicercaMap.put(dataId, data);

		List<Voce<String>> tipiIntervallo = new ArrayList<Voce<String>>(); //tipoIntervallo.ORARIO.label
		tipiIntervallo.add(new Voce<String>(Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.ORARIO.name()+".label"),
				Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.ORARIO.name()+".value")));
		tipiIntervallo.add(new Voce<String>(Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.GIORNALIERO.name()+".label"),
				Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.GIORNALIERO.name()+".value")));
		tipiIntervallo.add(new Voce<String>(Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.MENSILE.name()+".label"),
				Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.MENSILE.name()+".value")));
		String tipoIntervalloLabel = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo.label");
		SelectList<String> tipoIntervallo = new SelectList<String>(tipoIntervalloId, tipoIntervalloLabel, 
				Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.GIORNALIERO.name()+".value"), false, false, true, tipiIntervallo );
		this.infoRicercaMap.put(tipoIntervalloId, tipoIntervallo);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, ParamField<?>> getInfoGrafico(UriInfo uriInfo, BasicBD bd, boolean visualizzaRicerca,
			Map<String, String> parameters) throws ConsoleException {
		Map<String, ParamField<?>> infoGrafico = new HashMap<String, ParamField<?>>();

		if(this.infoRicercaMap == null){
			this.getInfoRicerca(uriInfo, bd);
		}

		String dataId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.data.id");
		String colonneId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.colonne.id");
		String tipoIntervalloId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo.id");

		InputNumber colonne = (InputNumber) this.infoRicercaMap.get(colonneId);
		colonne.setDefaultValue(null);
		infoGrafico.put(colonneId, colonne);

		InputDate data = (InputDate) this.infoRicercaMap.get(dataId);
		data.setDefaultValue(new Date());
		infoGrafico.put(dataId,data);

		SelectList<String> tipoIntervallo = (SelectList<String>) this.infoRicercaMap.get(tipoIntervalloId);
		tipoIntervallo.setDefaultValue(Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.GIORNALIERO.name()+".value"));
		infoGrafico.put(tipoIntervalloId,tipoIntervallo); 

		return infoGrafico;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, ParamField<?>> valorizzaInfoGrafico(UriInfo uriInfo, BasicBD bd, StatisticaFilter filter,Map<String, ParamField<?>> infoGrafico) throws ConsoleException {

		if(this.infoRicercaMap == null){
			this.getInfoRicerca(uriInfo, bd);
		}

		String dataId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.data.id");
		String colonneId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.colonne.id");
		String tipoIntervalloId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo.id");

		InputNumber colonne = (InputNumber) this.infoRicercaMap.get(colonneId);
		colonne.setDefaultValue(filter.getLimit());

		InputDate data = (InputDate) this.infoRicercaMap.get(dataId);
		data.setDefaultValue(filter.getData());

		SelectList<String> tipoIntervallo = (SelectList<String>) this.infoRicercaMap.get(tipoIntervalloId);
		tipoIntervallo.setDefaultValue(this.fromTipoIntervallo(filter.getTipoIntervallo()));

		return infoGrafico;
	}

	protected TipoIntervallo toTipoIntervallo(String tipoIntervalloS) {
		if(StringUtils.isNotEmpty(tipoIntervalloS)){
			if(tipoIntervalloS.equals(Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.ORARIO.name()+".value")))
				return TipoIntervallo.ORARIO;
			else if(tipoIntervalloS.equals(Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.GIORNALIERO.name()+".value")))
				return TipoIntervallo.GIORNALIERO;
			else if(tipoIntervalloS.equals(Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.MENSILE.name()+".value")))
				return TipoIntervallo.MENSILE;
		}
		return TipoIntervallo.GIORNALIERO;
	}

	protected String fromTipoIntervallo(TipoIntervallo tipoIntervallo) {
		switch (tipoIntervallo) {
		case MENSILE:
			return Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.MENSILE.name()+".value");
		case ORARIO:
			return Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.ORARIO.name()+".value");
		case GIORNALIERO:
		default:
			return Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.GIORNALIERO.name()+".value");
		}
	}
}
