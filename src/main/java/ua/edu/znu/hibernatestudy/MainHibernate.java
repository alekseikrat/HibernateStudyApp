package ua.edu.znu.hibernatestudy;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.exception.ConstraintViolationException;
import ua.edu.znu.hibernatestudy.entities.Tour;
import ua.edu.znu.hibernatestudy.entities.Client;
import ua.edu.znu.hibernatestudy.entities.Employee;
import ua.edu.znu.hibernatestudy.utils.HibernateJPAUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MainHibernate {
    public static void main(String[] args) {
        /*Get Session instance*/
        try (Session session = HibernateJPAUtils.getSessionFactory()
                .openSession()) {
            /*Create objects to insert into database*/
            Tour tour = new Tour();                //transient
            tour.setName("Одеса - Софія");
            Employee employee = new Employee();    //transient
            employee.setName("Іван");
            employee.setSurname("Якименко");
            employee.setAge(35);
            Client client = new Client();          //transient
            client.setName("Олексій");
            client.setSurname("Крат");
            client.setAge(24);
            Set<Tour> tours = new LinkedHashSet<>();
            tours.add(tour);
            client.setTours(tours);
            employee.setTour(tour);
            /*INSERT*/
            session.beginTransaction();
            Long employeeId = (Long) session.save(employee);  //persistent
            Long clientId = (Long) session.save(client);      //persistent
            Long tourId = (Long) session.save(tour);          //persistent
            session.getTransaction().commit();
            /*employeeId = 1, clientId= 1, tourId= 1*/
            System.out.println("Employee with id=" + employeeId
                    + ", client with id=" + clientId
                    + ", tour with id=" + tourId + " were inserted");
            System.out.println("Database info:\n"
                    + "Employee: " + session.load(Employee.class, 1L) + "\n" //persistent
                    + "Client: " + session.get(Client.class, 1L) + "\n"   //persistent
                    + "Tour: " + session.load(Tour.class, 1L));         //persistent
            /*UPDATE*/
            /*Update existing tour*/
            session.evict(tour); //detached
            tour.setName("Одеса - Афіни");
            /*Add another tour*/
            Tour newTour = new Tour(); //transient
            newTour.setName("Львів - Прага");
            session.beginTransaction();
            session.update(tour); //persistent
// session.update(newTour); //throw org.hibernate.TransientObjectException
            /*Universal - UPDATE OR INSERT IF ABSENT*/
            session.saveOrUpdate(newTour); //persistent
            session.getTransaction().commit();
            /*Get all tours*/
            List<Tour> allTours = HibernateJPAUtils
                    .getAllInstances(Tour.class, session);
            System.out.println("Tours info after update:");
            for (final Tour t : allTours) {
                System.out.println(t);
            }
            /*DELETE*/
            Tour tourToDelete = session.get(Tour.class, 2L); //persistent
            session.beginTransaction();
            session.delete(tourToDelete); //transient
            session.getTransaction().commit();
            allTours = HibernateJPAUtils.getAllInstances(Tour.class, session);
            System.out.println("Tours info after delete:");
            for (final Tour t : allTours) {
                System.out.println(t);
            }
        } catch (
                ConstraintViolationException e) {
            System.out.println("Duplicate insert!!! " + e.getMessage());
        } catch (
                HibernateException e) {
            e.printStackTrace();
        }
    }
}
