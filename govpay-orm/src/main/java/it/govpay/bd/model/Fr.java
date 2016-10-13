/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2016 Link.it srl (http://www.link.it).
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
package it.govpay.bd.model;

import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.bd.BasicBD;
import it.govpay.bd.anagrafica.AnagraficaManager;

public class Fr extends it.govpay.model.Fr{
	private static final long serialVersionUID = 1L;

	// Business
	private Dominio dominio;
	private Psp psp;
	
	public Dominio getDominio(BasicBD bd) throws ServiceException {
		if(dominio == null){
			dominio = AnagraficaManager.getDominio(bd, this.getIdDominio());
		}
		return dominio;
	}
	public void setDominio(Dominio dominio) {
		this.dominio = dominio;
	}

	public Psp getPsp(BasicBD bd) throws ServiceException {
		if(psp == null){
			psp = AnagraficaManager.getPsp(bd, this.getIdPsp());
		}
		return psp;
	}
	public void setPsp(Psp psp) {
		this.psp = psp;
	}
}
