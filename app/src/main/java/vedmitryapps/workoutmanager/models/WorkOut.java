package vedmitryapps.workoutmanager.models;


import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class WorkOut extends RealmObject {

    @PrimaryKey
    long id;
    String name;
    RealmList<Exercise> excersices;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<Exercise> getExcersices() {
        return excersices;
    }

    public void setExcersices(RealmList<Exercise> excersices) {
        this.excersices = excersices;
    }
}
