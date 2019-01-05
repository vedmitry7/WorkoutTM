package vedmitryapps.workoutmanager.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GodObject extends RealmObject {

    @PrimaryKey
    int id;
    RealmList<WorkOut> workouts;

    public RealmList<WorkOut> getWorkouts() {
        return workouts;
    }

    public void setWorkouts(RealmList<WorkOut> workouts) {
        this.workouts = workouts;
    }
}
