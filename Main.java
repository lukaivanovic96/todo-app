public class Main {
    public static void main(String[] args) {
        TaskRepository repo = new TaskRepository();

        repo.save("Kupiti mleko");
        repo.save("Nauciti Kafku");
        repo.save("Zavrsiti todo app");

        System.out.println("-- Sve:");
        repo.findAll().forEach(System.out::println);

        repo.update(2, "Nauciti Kafku", true);

        System.out.println("-- Posle update:");
        repo.findAll().forEach(System.out::println);

        repo.deleteById(1);

        System.out.println("-- Posle delete:");
        repo.findAll().forEach(System.out::println);
    }
}
