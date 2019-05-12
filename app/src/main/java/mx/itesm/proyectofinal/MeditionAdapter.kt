/*
    Copyright (C) 2018 - ITESM

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mx.itesm.proyectofinal

import Database.Medicion
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.row.view.*
import mx.itesm.proyectofinal.Utils.CustomItemClickListener

// Declares the adapter for the RecyclerView in PatientsList
class MeditionAdapter(var context: Context,
                      val listener: CustomItemClickListener): RecyclerView.Adapter<MeditionAdapter.MeditionViewHolder>() {

    lateinit var medicion:Medicion
    var numberOfItems = 0
    var mediciones:List<Medicion>? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): MeditionAdapter.MeditionViewHolder {
        val medicionViewHolder: MeditionAdapter.MeditionViewHolder

        val rowView = LayoutInflater.from(viewGroup.context).inflate(R.layout.row, viewGroup, false)

        medicionViewHolder = MeditionViewHolder(rowView)
        return medicionViewHolder
    }

    override fun getItemCount(): Int {
        return numberOfItems
    }

    override fun onBindViewHolder(p0: MeditionViewHolder, p1: Int) {
        p0.bind(p1)
    }

    fun setMedicion(medicionList: List<Medicion>){
        mediciones = medicionList
        numberOfItems = medicionList.size
    }

    inner class MeditionViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init { itemView.setOnClickListener(this) }

        fun bind (index:Int) {
            medicion = mediciones?.get(index)!!

            if(medicion.verificado!!){
                itemView.card_verified.text = "Medicion verificada"
            } else {
                itemView.card_verified.text = ""
            }

            //itemView.card_name.text = medicion.iniciales
            itemView.card_date.text = "Fecha Med: "+medicion.fecha
            itemView.card_calc.text = medicion.appSistolica.toString() + "/" + medicion.appDiastolica.toString()
            itemView.card_manual.text = medicion.manSistolica.toString() + "/" + medicion.manDiastolica.toString()
        }

        override fun onClick(v: View?) {
            val medicionesLocal = mediciones?.get(adapterPosition)
            listener.onCustomItemClick(medicionesLocal!!)
        }
    }
}