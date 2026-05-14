import java.util.ArrayList;
import java.util.List;

public class TodoRepository {

    private final List<Todo> todos = new ArrayList<>();
    private int nextId = 1;

    public Todo save(String title) {
        Todo todo = new Todo(nextId++, title);
        todos.add(todo);
        return todo;
    }

    public List<Todo> findAll() {
        return todos;
    }

    public Todo findById(int id) {
        return todos.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public boolean update(int id, String title, boolean completed) {
        Todo todo = findById(id);
        if (todo == null) return false;
        todo.setTitle(title);
        todo.setCompleted(completed);
        return true;
    }

    public boolean deleteById(int id) {
        return todos.removeIf(t -> t.getId() == id);
    }
}
