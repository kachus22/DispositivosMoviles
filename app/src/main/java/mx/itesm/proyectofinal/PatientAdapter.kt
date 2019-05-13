package mx.itesm.proyectofinal

import Database.Patient
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.row_patient.view.*
import mx.itesm.proyectofinal.Utils.CustomItemClickListener2


class PatientAdapter(var context: Context,
                     val listener: CustomItemClickListener2): RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    lateinit var patient:Patient
    var numberOfItems = 0
    var patients:List<Patient>? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): PatientViewHolder {
        val patViewHolder: PatientViewHolder

        val rowView = LayoutInflater.from(viewGroup.context).inflate(R.layout.row_patient, viewGroup, false)

        patViewHolder = PatientViewHolder(rowView)
        return patViewHolder
    }

    override fun getItemCount(): Int {
        return numberOfItems
    }

    override fun onBindViewHolder(p0: PatientViewHolder, p1: Int) {
        p0.bind(p1)
    }

    fun setPatient(patientsList: List<Patient>){
        patients = patientsList
        numberOfItems = patientsList.size
    }

    inner class PatientViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init { itemView.setOnClickListener(this) }

        fun bind (index:Int) {
            patient = patients?.get(index)!!

            //itemView.card_name.text = medicion.iniciales
            itemView.card_name.text = patient.FNameP+" "+patient.LNameP
            itemView.card_age.text = "Edad: "+patient.ageP.toString()
            itemView.card_gender.text = "Genero: "+patient.sexP
        }

        override fun onClick(v: View?) {
            val patientsLocal = patients?.get(adapterPosition)
            listener.onCustomItemClick(patientsLocal!!)
        }
    }
}