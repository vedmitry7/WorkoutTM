package vedmitryapps.workoutmanager;


import vedmitryapps.workoutmanager.models.Exercise;

public class Events {

    public static class OpenWorkout {
        private long id;
        public OpenWorkout(long id) {
            this.id = id;
        }
        public long getId() {
            return id;
        }
    }

    public static class StartWorkout {
        private long id;
        private int startTime;
        public StartWorkout(long id, int startTime) {
            this.id = id;
            this.startTime = startTime;
        }
        public long getId() {
            return id;
        }

        public int getStartTime() {
            return startTime;
        }
    }

    public static class PauseWorkout {
        private long id;
        public PauseWorkout(long id) {
            this.id = id;
        }
        public long getId() {
            return id;
        }
    }

    public static class StopWorkout {
        private long id;
        public StopWorkout(long id) {
            this.id = id;
        }
        public long getId() {
            return id;
        }
    }

    public static class UpdateWorkout {
        private long id;
        public UpdateWorkout(long id) {
            this.id = id;
        }
        public long getId() {
            return id;
        }
    }

    public static class UpdateWorkoutSticky {
        private long id;
        public UpdateWorkoutSticky(long id) {
            this.id = id;
        }
        public long getId() {
            return id;
        }
    }

    public static class WorkoutStep {
        private long id;
        private int time;
        private boolean finished;
        private boolean paused;
        private boolean interrupted;
        private int repeating;

        public WorkoutStep(long id, int time) {
            this.id = id;
            this.time = time;
        }
        public long getId() {
            return id;
        }

        public int getTime() {
            return time;
        }

        public boolean isFinished() {
            return finished;
        }

        public void setFinished(boolean finished) {
            this.finished = finished;
        }

        public boolean isPaused() {
            return paused;
        }

        public void setPaused(boolean paused) {
            this.paused = paused;
        }

        public boolean isInterrupted() {
            return interrupted;
        }

        public void setInterrupted(boolean interrupted) {
            this.interrupted = interrupted;
        }

        public int getRepeating() {
            return repeating;
        }

        public void setRepeating(int repeating) {
            this.repeating = repeating;
        }
    }

    public static class ClickExercise {
        private int position;
        public ClickExercise(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }
    }

    public static class ChooseSound {
        Exercise exercise;

        public ChooseSound(Exercise exercise) {
            this.exercise = exercise;
        }

        public Exercise getExercise() {
            return exercise;
        }

        public void setExercise(Exercise exercise) {
            this.exercise = exercise;
        }
    }


    public static class DeleteWorkout {
        private int position;
        public DeleteWorkout(int adapterPosition) {
        position = adapterPosition;
        }

        public int getPosition() {
            return position;
        }
    }

    public static class DeleteFromFinished {
        Long workoutId;
        public DeleteFromFinished(Long workoutId) {
            this.workoutId = workoutId;
        }
    }

    public static class Repeating {
        long id;
        int repeatingCount;
        public Repeating(long id, int repeatingCount) {
            this.id = id;
            this.repeatingCount = repeatingCount;
        }

        public long getId() {
            return id;
        }

        public int getRepeatingCount() {
            return repeatingCount;
        }
    }

    public static class OpenSettings {
    }

    public static class SetStatusBar {
    }

    public static class RemoveAds {

    }
}
