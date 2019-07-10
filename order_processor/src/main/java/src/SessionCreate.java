package src;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import scala.Serializable;

public class SessionCreate  {
    static private StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
            .configure()
            .build();
    static private SessionFactory sessionFactory =new MetadataSources( registry ).buildMetadata().buildSessionFactory();
    static public Session getSession()
    {
        return sessionFactory.openSession();
    }
}
