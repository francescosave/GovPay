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
package it.govpay.web.rs.dars.anagrafica.ruoli.input;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.bd.BasicBD;
import it.govpay.bd.anagrafica.RuoliBD;
import it.govpay.model.Acl;
import it.govpay.model.Acl.Servizio;
import it.govpay.model.Acl.Tipo;
import it.govpay.model.Ruolo;
import it.govpay.web.rs.dars.model.RawParamValue;
import it.govpay.web.rs.dars.model.Voce;
import it.govpay.web.rs.dars.model.input.dinamic.SelectList;
import it.govpay.web.utils.Utils;

public class DirittiFunzionalita_A_CON extends SelectList<Long>{

	private String funzionalita_A_CONId= null;
	private String ruoloId = null;
	private String nomeServizio = null;
	private Servizio servizio = Servizio.Anagrafica_Contabile;
	private Tipo tipo = Tipo.DOMINIO;

	public DirittiFunzionalita_A_CON(String nomeServizio,String id, String label, URI refreshUri, List<RawParamValue> paramValues,
			 Object... objects) {
		super(id, label, refreshUri, paramValues, objects);
		Locale locale = objects[1] != null ? (Locale) objects[1] : null;
		this.nomeServizio = nomeServizio;
		this.funzionalita_A_CONId = Utils.getInstance(locale).getMessageFromResourceBundle(this.nomeServizio + ".funzionalita_A_CON.id");
		this.ruoloId = Utils.getInstance(locale).getMessageFromResourceBundle(this.nomeServizio + ".id.id");
	}

	@Override
	protected List<Voce<Long>> getValues(List<RawParamValue> paramValues, Object... objects) throws ServiceException {
		String funzionalita_A_CONVAlue = Utils.getValue(paramValues, this.funzionalita_A_CONId);
		List<Voce<Long>> lst = new ArrayList<Voce<Long>>();
		Locale locale = objects[1] != null ? (Locale) objects[1] : null;
		
		if(StringUtils.isNotEmpty(funzionalita_A_CONVAlue) && funzionalita_A_CONVAlue.equalsIgnoreCase("false")){
			return lst;
		}
		
		lst.add(new Voce<Long>(Utils.getInstance(locale).getMessageFromResourceBundle(this.nomeServizio + ".diritto.NESSUNO.label"), (long) Ruolo.NO_DIRITTI));
		lst.add(new Voce<Long>(Utils.getInstance(locale).getMessageFromResourceBundle(this.nomeServizio + ".diritto.LETTURA.label"), (long)  Ruolo.DIRITTI_LETTURA));
		lst.add(new Voce<Long>(Utils.getInstance(locale).getMessageFromResourceBundle(this.nomeServizio + ".diritto.SCRITTURA.label"), (long)  Ruolo.DIRITTI_SCRITTURA));
		return lst;
	}

	@Override
	protected Long getDefaultValue(List<RawParamValue> values, Object... objects) {
		String funzionalita_A_CONVAlue = Utils.getValue(values, this.funzionalita_A_CONId);
		String idRuolo = Utils.getValue(values, this.ruoloId);
		List<Integer> lst = new ArrayList<Integer>();

		if(StringUtils.isNotEmpty(funzionalita_A_CONVAlue) && funzionalita_A_CONVAlue.equalsIgnoreCase("false")){
			return (long) Ruolo.NO_DIRITTI;
		}
		if(StringUtils.isEmpty(idRuolo)){
			return (long) Ruolo.NO_DIRITTI;
		}

		try {
			BasicBD bd = (BasicBD) objects[0];
			RuoliBD ruoliBD = new RuoliBD(bd);
			Ruolo portale = ruoliBD.getRuolo(Long.parseLong(idRuolo));
			List<Acl> acls = portale.getAcls();
			
			for (Acl acl : acls) {
				Tipo tipo = acl.getTipo();
				if(acl.getServizio().equals(this.servizio) && tipo.equals(this.tipo)){
					lst.add(acl.getDiritti());
				}
			}
			
		} catch (Exception e) {
		}

		if(lst.size() > 0 )
			return lst.get(0).longValue();
		else 
			return (long) Ruolo.NO_DIRITTI;
	}
	@Override
	protected boolean isRequired(List<RawParamValue> values, Object... objects) {
		String funzionalita_A_CONVAlue = Utils.getValue(values, this.funzionalita_A_CONId);

		if(StringUtils.isNotEmpty(funzionalita_A_CONVAlue) && funzionalita_A_CONVAlue.equalsIgnoreCase("false")){
			return false;
		}

		return true;
	}
	@Override
	protected boolean isHidden(List<RawParamValue> values, Object... objects) {
		String funzionalita_A_CONVAlue = Utils.getValue(values, this.funzionalita_A_CONId);
		if(StringUtils.isNotEmpty(funzionalita_A_CONVAlue) && funzionalita_A_CONVAlue.equalsIgnoreCase("false")){
			return true;
		}

		return false;
	}
	@Override
	protected boolean isEditable(List<RawParamValue> values, Object... objects) {
		String funzionalita_A_CONVAlue = Utils.getValue(values, this.funzionalita_A_CONId);
		if(StringUtils.isNotEmpty(funzionalita_A_CONVAlue) && funzionalita_A_CONVAlue.equalsIgnoreCase("false")){
			return false;
		}
		return true;
	}

}