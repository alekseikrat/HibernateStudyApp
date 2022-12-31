package ua.edu.znu.hibernatestudy.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;

import java.util.List;

public class HibernateJPAUtils {
    public static SessionFactory getSessionFactory() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure()
                .build();
        SessionFactory sessionFactory = new MetadataSources(registry)
                .buildMetadata()
                .buildSessionFactory();
        return sessionFactory;
    }
    public static <T> List<T> getAllInstances(Class<T> entity, Session session)
    {
        Query query = session.createQuery("from " + entity.getSimpleName());
        List<T> allInstances = query.list();
        return allInstances;
    }
}
