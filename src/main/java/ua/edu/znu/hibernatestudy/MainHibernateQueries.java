package ua.edu.znu.hibernatestudy;

import jakarta.persistence.criteria.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import ua.edu.znu.hibernatestudy.entities.Tour;
import ua.edu.znu.hibernatestudy.entities.Client;
import ua.edu.znu.hibernatestudy.entities.Employee;
import ua.edu.znu.hibernatestudy.utils.HibernateJPAUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Using different Query strategy for data retrieving from database.
 */
public class MainHibernateQueries {
    public static void main(String[] args) {
        /*Get Session instance*/
        try (Session session =
                     HibernateJPAUtils.getSessionFactory().openSession()) {
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
            session.beginTransaction();
            Long employeeId = (Long) session.save(employee);  //persistent
            Long clientId = (Long) session.save(client);      //persistent
            Long tourId = (Long) session.save(tour);
            session.getTransaction().commit();
            /*employeeId = 1, clientId= 1, tourId= 1*/
            System.out.println("employeeId = " + employeeId
                    + ", clientId= " + clientId + ", tourId= " + tourId);

            /*SELECT ALL*/
            /*Using native SQL query*/
            NativeQuery sqlQuery = session.createNativeQuery("SELECT * FROM client");
            sqlQuery.addEntity(Client.class);
            final List<Client> allClients = sqlQuery.list();
            System.out.println("Clients info (native SQL):");
            allClients.forEach(System.out::println);
            /*Using Hibernate Query Language - HQL*/
            Query query = session.createQuery("from Employee");
            final List<Employee> allEmployees = query.list();
            System.out.println("Employees info (HQL):");
            allEmployees.forEach(System.out::println);
            /*Using Criteria API*/
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Tour> criteriaQuery = builder.createQuery(Tour.class);
            Root<Tour> root = criteriaQuery.from(Tour.class);
            criteriaQuery.select(root);
            Query<Tour> allToursQuery = session.createQuery(criteriaQuery);
            List<Tour> allTours = allToursQuery.getResultList();
            System.out.println("Tours info (Criteria API):");
            allTours.forEach(System.out::println);

            /*SELECT WHERE*/
            /*Using native SQL query - prepared query*/
            String clientName = "Олексій";
            String clientSurname = "Крат";
            sqlQuery = session.createNativeQuery("SELECT * FROM client " +
                            "WHERE name = :clientName AND surname = :clientSurname")
                    .addEntity(Client.class)
                    .setParameter("clientName", clientName)
                    .setParameter("clientSurname", clientSurname);
            Client foundedClient = (Client) sqlQuery.uniqueResult();
            System.out.println("Info for client (native SQL): "
                    + clientName + " " + clientSurname + ":");
            System.out.println(foundedClient);
            /*Using Hibernate Query Language - HQL*/
            int employeeAge = 35;
            query = session.createQuery("from Employee e where e.age=:employeeAge")
                    .setParameter("employeeAge", employeeAge);
            Employee foundedEmployee = (Employee) query.uniqueResult();
            System.out.println("Search employee with age " + employeeAge + " (HQL):");
            System.out.println(foundedEmployee);
            /*Using Criteria API*/
            String tourName = "Одеса - Софія";
            System.out.println("Search tour with name " + tourName + " (Criteria API):");
            builder = session.getCriteriaBuilder();
            criteriaQuery = builder.createQuery(Tour.class);
            root = criteriaQuery.from(Tour.class);
            ParameterExpression<String> paramTourName = builder.parameter(String.class);
            Expression<String> tourNameExpression = root.get("name");
            Predicate tourNameEqual = builder.equal(tourNameExpression, paramTourName);
            criteriaQuery.select(root).where(tourNameEqual);
            Query<Tour> singleTourQuery = session.createQuery(criteriaQuery);
            singleTourQuery.setParameter(paramTourName, tourName);
            Tour foundedTour = singleTourQuery.uniqueResult();
            System.out.println(foundedTour);

            /*UPDATE WHERE*/
            /*We need transaction for update, delete and insert queries*/
            session.beginTransaction();
            /*Using native SQL query - prepared query*/
            tourName = "Львів - Прага";
            int queryTourId = 1;
            sqlQuery = session.createNativeQuery("UPDATE tour SET name = :tourName"
                    + " WHERE id = :tourId");
            sqlQuery.addEntity(Tour.class);
            sqlQuery.setParameter("tourName", tourName).setParameter("tourId", queryTourId);
            int result = sqlQuery.executeUpdate();
            System.out.println("Updated by SQL query " + result + " rows.");
            /*Using Hibernate Query Language - HQL*/
            employeeAge = 35;
            String employeeName = "Віктор";
            query = session.createQuery("update Employee set name=:employeeName"
                    + " where age=:employeeAge");
            query.setParameter("employeeName", employeeName)
                    .setParameter("employeeAge", employeeAge);
            result = query.executeUpdate();
            System.out.println("Updated by HQL query " + result + " rows.");
            /*Using Criteria API*/
            clientSurname = "Крат";
            int clientNewAge = 24;
            builder = session.getCriteriaBuilder();
            CriteriaUpdate<Client> updateCriteriaQuery =
                    builder.createCriteriaUpdate(Client.class);
            Root<Client> clientRoot = updateCriteriaQuery.from(Client.class);
            updateCriteriaQuery.set("age", clientNewAge);
            updateCriteriaQuery.where(builder.equal(clientRoot.get("surname"),
                    clientSurname));
            Query<Client> updateClientQuery = session.createQuery(updateCriteriaQuery);
            result = updateClientQuery.executeUpdate();
            System.out.println("Updated by Criteria query " + result + " rows.");
            session.getTransaction().commit();

            /*DELETE WHERE*/
            session.beginTransaction();
            /*Using native SQL query - prepared query*/
            queryTourId = 1;
            sqlQuery = session.createNativeQuery("DELETE FROM clients_tours "
                    + "WHERE tour_id = :tourId");
            sqlQuery.setParameter("tourId", queryTourId);
            result = sqlQuery.executeUpdate();
            System.out.println("Deleted by SQL query " + result + " rows.");
            /*Using Hibernate Query Language - HQL*/
            employeeName = "Віктор";
            query = session.createQuery("delete from Employee where name=:employeeName");
            query.setParameter("employeeName", employeeName);
            result = query.executeUpdate();
            System.out.println("Deleted by HQL query " + result + " rows.");
            /*Using Criteria API*/
            clientSurname = "Крат";
            builder = session.getCriteriaBuilder();
            CriteriaDelete<Client> deleteCriteriaQuery =
                    builder.createCriteriaDelete(Client.class);
            clientRoot = deleteCriteriaQuery.from(Client.class);
            deleteCriteriaQuery.where(builder.equal(clientRoot.get("surname"),
                    clientSurname));
            Query<Client> deleteClientQuery = session.createQuery(deleteCriteriaQuery);
            result = deleteClientQuery.executeUpdate();
            System.out.println("Deleted by Criteria query " + result + " rows.");
            session.getTransaction().commit();
        } catch (ConstraintViolationException e) {
            System.out.println("Duplicate insert!!! " + e.getMessage());
        } catch (HibernateException e) {
            /*При возникновении ошибки в окне выводится сообщение*/
            e.printStackTrace();
        }
    }
}