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

    public static class ResumeWorkout {
        private long id;
        public ResumeWorkout(long id) {
            this.id = id;
        }
        public long getId() {
            return id;
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
        private boolean itterapted;
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

        public boolean isItterapted() {
            return itterapted;
        }

        public void setItterapted(boolean itterapted) {
            this.itterapted = itterapted;
        }

        public int getRepeating() {
            return repeating;
        }

        public void setRepeating(int repeating) {
            this.repeating = repeating;
        }
    }

    public static class SelectedTrainingMessage {
        private String message;
        public SelectedTrainingMessage(String message) {
            this.message = message;
        }
        public String getMessage() {
            return message;
        }
    }


    public static class ServiceMainFragmentTrainingProgressMessage {
        @Override
        public String toString() {
            return  currentProgressTime +
                    " SEC. " + currentPeriodName + ' ' + currentPeriodTime +
                    " sec. Total " + totalTrainingTime +
                    ", PeriodId =" + currentPeriodId +
                    ", totalCurrentPeriodTime=" + totalCurrentPeriodTime +
                    ", pause=" + pause +
                    '}';
        }

        private long id;
        private int currentProgressTime;
        private String currentPeriodName;
        private boolean finish;
        private int currentPeriodTime;
        private int totalTrainingTime;
        private int totalCurrentPeriodTime;
        private long currentPeriodId;
        private boolean pause;
        private boolean fromMain;

        public boolean isFromMain() {
            return fromMain;
        }

        public void setFromMain(boolean fromMain) {
            this.fromMain = fromMain;
        }

        public ServiceMainFragmentTrainingProgressMessage(long id, int  currentProgressTime, int totalTrainingTime, long currentPeriodId, String currentPeriodName, int currentPeriodTime, int totalCurrentPeriodTime, boolean finish) {
            this.id = id;
            this.currentProgressTime = currentProgressTime;
            this.currentPeriodName = currentPeriodName;
            this.currentPeriodTime = currentPeriodTime;
            this.totalTrainingTime = totalTrainingTime;
            this.totalCurrentPeriodTime = totalCurrentPeriodTime;
            this.finish = finish;
            this.currentPeriodId = currentPeriodId;
        }
        public int getCurrentProgressTime() {
            return currentProgressTime;
        }
        public long getId() {
            return id;
        }
        public String getCurrentPeriodName() {
            return currentPeriodName;
        }
        public int getTotalTrainingTime() {
            return totalTrainingTime;
        }
        public int getTotalCurrentPeriodTime() {
            return totalCurrentPeriodTime;
        }
        public long getCurrentPeriodId() {
            return currentPeriodId;
        }
        public boolean isPause() {
            return pause;
        }
        public void setPause(boolean pause) {
            this.pause = pause;
        }
        public int getCurrentPeriodTime() {
            return currentPeriodTime;
        }
        public boolean isFinish() {
            return finish;
        }
    }

    public static class PlayMessage {
        private long id;

        public PlayMessage(long id) {
            this.id = id;
        }

        public long getTrainingId() {
            return id;
        }
    }

    public static class PauseMessage {
        private long id;

        public PauseMessage(long id) {
            this.id = id;
        }

        public long getTrainingId() {
            return id;
        }
    }

    public static class StopMessage {
        private long id;

        public StopMessage(long id) {
            this.id = id;
        }

        public long getTrainingId() {
            return id;
        }
    }

    public static class StopUpdate {
        private boolean update;

        public boolean isUpdate() {
            return update;
        }

        public StopUpdate(boolean update) {
            this.update = update;
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

    public static class ChangeExercise {
        Exercise exercise;

        public ChangeExercise(Exercise exercise) {
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
}
