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

// Declares test data for the application.
class MedicionData(val context: Context) {

    var listaMedicion: ArrayList<Medicion> = ArrayList()

    init {
        dataList()
    }

    fun dataList() {
        listaMedicion.add(Medicion("121",
                "81",
                "116",
                "64",
                "08/12/2018",
                true,
                "I",
                null,
                "JMP"))
        listaMedicion.add(Medicion("122",
                "82",
                "117",
                "65",
                "12/03/2018",
                false,
                "I",
                null,
                "GV"))
        listaMedicion.add(Medicion("123",
                "82",
                "118",
                "66",
                "04/06/2018",
                true,
                "D",
                null,
                "RV"))
    }
}