import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    private final List<Task> tasks = new ArrayList<>();
    private int nextId = 1;

    public Task save(String title) {
        Task task = new Task(nextId++, title);
        tasks.add(task);
        return task;
    }

    public List<Task> findAll() {
        return tasks;
    }

    public Task findById(int id) {
        return tasks.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public boolean update(int id, String title, boolean completed) {
        Task task = findById(id);
        if (task == null) return false;
        task.setTitle(title);
        task.setCompleted(completed);
        return true;
    }

    public boolean deleteById(int id) {
        return tasks.removeIf(t -> t.getId() == id);
    }
}
