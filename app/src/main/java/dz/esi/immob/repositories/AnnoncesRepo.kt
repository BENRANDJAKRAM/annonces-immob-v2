package dz.esi.immob.repositories

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source

class AnnoncesRepo private constructor(){

    companion object{
        val instance = AnnoncesRepo()
    }

    var mFeed = MutableLiveData<List<Annonce>>()
    val db = FirebaseFirestore.getInstance()

    var userRepo = UserData.instance

    init {
        getFeed()
    }

    fun getFeed(): MutableLiveData<List<Annonce>> {
        if (mFeed.value == null) {
            db.collection("annonces")
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }
                    val annoces = value?.toObjects(Annonce::class.java)
                    userRepo.getFavAnnonces().value?.forEach{fav ->
                        annoces?.forEach { annonce ->
                            if(fav.id == annonce.id){
                                annonce.favorite = 1
                            }
                        }
                    }
                    mFeed.postValue(annoces)
                }
        }
        return mFeed
    }

    fun filter(criteria: Map<String, Any>): MutableLiveData<List<Annonce>>{
        val ref = db.collection("annonces")
        var query: Query? = null
        val annonces = MutableLiveData<List<Annonce>>()

        for((key, value) in criteria){
            query = ref.whereEqualTo(key, value)
        }
        query?.get(Source.CACHE)
            ?.addOnSuccessListener {
                annonces.postValue(it.toObjects(Annonce::class.java))
            }
        return annonces
    }

}