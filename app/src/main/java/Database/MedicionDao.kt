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

package Database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.Dao

// Data handling of the information in the database
@Dao
interface MedicionDao {
    @Query("SELECT * FROM Medicion WHERE iniciales = :paciente")
    fun cargarMeciciones(paciente : String): LiveData<List<Medicion>>

    @Query("SELECT COUNT (*) FROM Medicion WHERE iniciales = :paciente")
    fun getAnyMedicion(paciente: String):Int

    @Insert
    fun insertarMedicion(medicion: Medicion)

    @Insert
    fun insertartListaMediciones(mediciones: List<Medicion>)

    @Query("DELETE FROM Medicion WHERE _id = :id")
    fun borrarMedicion(id: Int)

    @Query("SELECT * FROM Medicion WHERE _id = :id")
    fun cargarMedicionId(id: Int): LiveData<Medicion>

    //Cargar medicion por Id devuelve una medicion
    @Query("SELECT * FROM Medicion WHERE _id = :id")
    fun cargarMedicionPorId(id: Int): Medicion

    @Query ("UPDATE Medicion SET verificado = :verification WHERE _id = :id")
    fun updateMedicion(id: Int, verification: Boolean)
}
