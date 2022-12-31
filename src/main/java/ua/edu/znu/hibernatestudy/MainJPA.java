package ua.edu.znu.hibernatestudy;

import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import ua.edu.znu.hibernatestudy.entities.Tour;
import ua.edu.znu.hibernatestudy.entities.Client;
import ua.edu.znu.hibernatestudy.entities.Employee;
import ua.edu.znu.hibernatestudy.utils.HibernateJPAUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MainJPA {
    private static EntityManagerFactory managerFactory = null;
    private static EntityManager entityManager = null;
    public static void main(String[] args) {
        /*Get Entity Manager instance*/
        try {
            managerFactory = Persistence
                    .createEntityManagerFactory("travel_agencyPU");
            entityManager = managerFactory.createEntityManager();
            /*Create objects to insert into database*/
            Tour tour = new Tour();
            tour.setName("Одеса - Софія");
            Employee employee = new Employee();
            employee.setName("Іван");
            employee.setSurname("Якименко");
            employee.setAge(35);
            Client client = new Client();
            client.setName("Олексій");
            client.setSurname("Крат");
            client.setAge(23);
            Set<Tour> tours = new LinkedHashSet<>();
            tours.add(tour);
            client.setTours(tours);
            employee.setTour(tour);
            /*INSERT*/
            entityManager.getTransaction().begin();
            entityManager.persist(employee);
            entityManager.persist(client);
            entityManager.persist(tour);
            entityManager.getTransaction().commit();
            System.out.println("Database info:\n"
                    + "Employee: " + entityManager.find(Employee.class, 1L) + "\n"
                    + "Client: " + entityManager.find(Client.class, 1L) + "\n"
                    + "Tour: " + entityManager.find(Tour.class, 1L));

            /*SELECT ALL*/
            /*Using native SQL query*/
            Query sqlQuery = entityManager.createNativeQuery("SELECT * "
                    + "FROM client", Client.class);
            final List<Client> allClients = sqlQuery.getResultList();
            System.out.println("Clients info (native SQL):");
            for (Client cli : allClients) {
                System.out.println(cli);
            }
            /*Using JPA Query Language - JPQL*/
            Query query = entityManager.createQuery("from Employee");
            final List<Employee> allEmployees = query.getResultList();
            System.out.println("Employees info (HQL):");
            for (Employee emp : allEmployees) {
                System.out.println(emp);
            }
            /*Using Criteria API*/
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Tour> criteriaQuery = builder.createQuery(Tour.class);
            Root<Tour> root = criteriaQuery.from(Tour.class);
            criteriaQuery.select(root);
            Query allToursQuery = entityManager.createQuery(criteriaQuery);
            List<Tour> allTours = allToursQuery.getResultList();
            System.out.println("Tours info (Criteria API):");
            for (final Tour t : allTours) {
                System.out.println(t);
            }

            /*SELECT WHERE*/
            /*Using native SQL query - prepared query*/
            String clientName = "Олексій";
            String clientSurname = "Крат";
            sqlQuery = entityManager.createNativeQuery("SELECT * "
                            + "FROM client WHERE name = :clientName "
                            + "AND surname = :clientSurname", Client.class)
                    .setParameter("clientName", clientName)
                    .setParameter("clientSurname", clientSurname);
            Client foundedClient = (Client) sqlQuery.getSingleResult();
            System.out.println("Info for client (native SQL): " + clientName
                    + " " + clientSurname + ":");
            System.out.println(foundedClient);
            /*Using JPA Query Language - JPQL*/
            int employeeAge = 35;
            query = entityManager.createQuery("from Employee e "
                            + "where e.age=:employeeAge")
                    .setParameter("employeeAge", employeeAge);
            Employee foundedEmployee = (Employee) query.getSingleResult();
            System.out.println("Search employee with age " + employeeAge
                    + " (HQL):");
            System.out.println(foundedEmployee);
            /*Using Criteria API*/
            String tourName = "Одеса - Софія";
            System.out.println("Search tour with name " + tourName
                    + " (Criteria API):");
            builder = entityManager.getCriteriaBuilder();
            criteriaQuery = builder.createQuery(Tour.class);
            root = criteriaQuery.from(Tour.class);
            ParameterExpression<String> paramTourName = builder.parameter(String.class);
            criteriaQuery.select(root)
                    .where(builder.equal(root.get("name"), paramTourName));
            Query singleTourQuery = entityManager.createQuery(criteriaQuery);
            singleTourQuery.setParameter(paramTourName, tourName);
            Tour foundedTour = (Tour) singleTourQuery.getSingleResult();
            System.out.println(foundedTour);

            /*UPDATE*/
            /*Update existing tour*/
            tour.setName("Одеса - Софія");
            /*Add another tour*/
            Tour newTour = new Tour(); //transient
            newTour.setName("Львів - Прага");
            entityManager.getTransaction().begin();
            entityManager.merge(tour); //persistent
            entityManager.merge(newTour); //persistent
            entityManager.getTransaction().commit();
            /*Get all tours*/
            List<Tour> allTour = HibernateJPAUtils.getAllInstances(Tour.class, (Session) entityManager);
            System.out.println("Tours info after update:");
            for (final Tour t : allTour) {
                System.out.println(t);
            }

            /*DELETE*/
            Tour tourToDelete = entityManager.find(Tour.class, 2L); //persistent
            entityManager.getTransaction().begin();
            entityManager.remove(tourToDelete); //transient
            entityManager.getTransaction().commit();
            allTour = HibernateJPAUtils.getAllInstances(Tour.class, (Session) entityManager);
            System.out.println("Tours info after delete:");
            for (final Tour t : allTours) {
                System.out.println(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
            managerFactory.close();
        }
    }
}