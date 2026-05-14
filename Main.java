public class Main {
    public static void main(String[] args) {
        Todo todo1 = new Todo(1, "Kupiti mleko");
        Todo todo2 = new Todo(2, "Nauciti Kafku");

        System.out.println(todo1);
        System.out.println(todo2);

        todo1.setCompleted(true);
        System.out.println(todo1);
    }
}
