h1. JPA et tests d’intégrations

from "Le Touilleur Express : JPA et tests d’intégrations":http://www.touilleur-express.fr/2010/08/06/jpa-et-tests-dintegrations/

6 AOÛT 2010 3 297 AFFICHAGES 22 COMMENTAIRES

Voici un article qui intéressera plus les débutants, et qui intéressera aussi ce qui cherchent un petit exemple afin de tester JPA 2, l’implémentation d’Hibernate 3.5 et  Apache Derby. Je vous parlerai aussi de la librairie Google Guava, qui permet de simplifier le code de son entité. Bref un petit article sans prétentions, à lire sur la plage.

{toc:style=disc|maxLevel=3}

h2. Maven

Voyons comment créer un petit projet simple avec JPA. J’utilise Apache Derby, une base en mémoire qui permet de tester du code simple sans avoir besoin d’une vraie base Oracle ou MySQL. Du côté des dépendances, je vous déconseille slf4j-simple. Prenez plutôt slf4j-log4j12 afin de pouvoir contrôler la verbosité des logs d’Hibernate. J’utilise Apache Derby 10.5.3.0, et j’ai ajouté le repository de JBoss.


bc.. 
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
  http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.letouilleur.sample</groupId>
    <artifactId>sample</artifactId>
    <packaging>jar</packaging>
    <version>1.0-SNAPSHOT</version>
    <name>Articles du Touilleur Express</name>
    <url>http://touilleur-express.fr/</url>
 
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>2.3.1</version>
                <configuration>
                    <source>1.6</source>
                    <target>1.6</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
 
    <repositories>
        <repository>
            <id>jboss</id>
            <name>JBoss repository</name>
            <url>http://repository.jboss.org/maven2</url>
        </repository>
    </repositories>
 
    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>3.8.1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>r06</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-entitymanager</artifactId>
            <version>3.3.2.GA</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.derby</groupId>
            <artifactId>derby</artifactId>
            <version>10.5.3.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-entitymanager</artifactId>
            <version>3.5.1-Final</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.5.6</version>
        </dependency>
    </dependencies>
 
</project>
	
h2. Persistence

Dans le répertoire testresourcesMETA-INF j’ai placé un fichier persistence.xml. Je déclare explicitement mon entité org.letouilleur.sample.Instrument

bc.. 
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="1.0"
             xmlns="http://java.sun.com/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd">
 
    <persistence-unit name="testPU" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.ejb.HibernatePersistence</provider>
        <class>org.letouilleur.sample.Instrument</class>
        <exclude-unlisted-classes>true</exclude-unlisted-classes>
        <properties>
            <property name="hibernate.connection.url" value="jdbc:derby:memory:unit-testing-jpa"/>
            <property name="hibernate.connection.driver_class" value="org.apache.derby.jdbc.EmbeddedDriver"/>
            <property name="hibernate.dialect" value="org.hibernate.dialect.DerbyDialect"/>
            <property name="hibernate.hbm2ddl.auto" value="create"/>
            <property name="hibernate.connection.username" value=""/>
            <property name="hibernate.connection.password" value=""/>
        </properties>
    </persistence-unit>
</persistence>

h2. Test avec JPA

Pour tester l’entité, j’ai fait le choix d’écrire le minimum de code et de ne pas utiliser Spring. J’ai créé une classe abstract pour tout ce qui est mise en marche du contexte de persistence, puis un test simple pour valider la création de mon entité. Pas de DAO non plus, l’EntityManager est un DAO à part entière non ?

Le test de base:

bc.. 
	package org.letouilleur.sample;
	 
	import junit.framework.TestCase;
	import org.apache.derby.impl.io.VFMemoryStorageFactory;
	 
	import javax.persistence.EntityManager;
	import javax.persistence.EntityManagerFactory;
	import javax.persistence.Persistence;
	import java.io.File;
	import java.sql.DriverManager;
	import java.sql.SQLNonTransientConnectionException;
	import java.util.logging.Logger;
	 
	/**
	 * A simple Persistence Unit test.
	 *
	 * @author Nicolas Martignole
	 * @since 5 août 2010 21:53:10
	 */
	public abstract class PersistenceTest extends TestCase {
	    private static Logger logger = Logger.getLogger(PersistenceTest.class.getName());
	    private EntityManagerFactory emFactory;
	    protected EntityManager em;
	 
	    public PersistenceTest(){
	 
	    }
	 
	    public PersistenceTest(String testName) {
	        super(testName);
	    }
	 
	    @Override
	    protected void setUp() throws Exception {
	        super.setUp();
	        try {
	            logger.info("Starting in-memory database for unit tests");
	            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
	            DriverManager.getConnection("jdbc:derby:memory:unit-testing-jpa;create=true").close();
	        } catch (Exception ex) {
	            ex.printStackTrace();
	            fail("Exception during database startup.");
	        }
	        try {
	            logger.info("Building JPA EntityManager for unit tests");
	            emFactory = Persistence.createEntityManagerFactory("testPU");
	            em = emFactory.createEntityManager();
	        } catch (Exception ex) {
	            ex.printStackTrace();
	            fail("Exception during JPA EntityManager instanciation.");
	        }
	    }
	 
	    @Override
	    protected void tearDown() throws Exception {
	        super.tearDown();
	        logger.info("Shuting down Hibernate JPA layer.");
	        if (em != null) {
	            em.close();
	        }
	        if (emFactory != null) {
	            emFactory.close();
	        }
	        logger.info("Stopping in-memory database.");
	        try {
	            DriverManager.getConnection("jdbc:derby:memory:unit-testing-jpa;shutdown=true").close();
	        } catch (SQLNonTransientConnectionException ex) {
	            if (ex.getErrorCode() != 45000) {
	                throw ex;
	            }
	            // Shutdown success
	        }
	        VFMemoryStorageFactory.purgeDatabase(new File("unit-testing-jpa").getCanonicalPath());
	    }
	 
	 
	}

p. Le test InstrumentJPATest me permet de tester la création de mon entité :

bc.. 
	package org.letouilleur.sample;
	 
	/**
	 * A simple JPA integration test to demonstrate equals and hashCode issues.
	 *
	 * @author Nicolas Martignole
	 * @since 5 août 2010 22:24:29
	 */
	public class InstrumentJPATest extends PersistenceTest {
	    public void testPersistence() {
	        try {
	            em.getTransaction().begin();
	 
	            Instrument instrument = new Instrument("FR1234567890");
	 
	            em.persist(instrument);
	            assertTrue(em.contains(instrument));
	 
	            em.remove(instrument);
	            assertFalse(em.contains(instrument));
	 
	            em.getTransaction().commit();
	 
	        } catch (Exception ex) {
	            em.getTransaction().rollback();
	            ex.printStackTrace();
	            fail("Exception during testPersistence");
	        }
	    }
	 
	}

h2. Entité

p. Nous sommes maintenant prêt à créer notre Entité. Je vous propose 2 versions : une simple et une différente avec Google Guava.

p. Dans la spécification JSR-317 de JPA2, notez les points suivants :

p. Extrait de la spécification JSR-317:

bq.. 
The entity class must be annotated with the Entity annotation or denoted in the XML descriptor as an entity.
	
	The entity class must have a no-arg constructor. The entity class may have other constructors as well. The no-arg constructor must be public or protected.
	
	The entity class must be a top-level class. An enum or interface must not be designated as an entity.
	
	The entity class must not be final. No methods or persistent instance variables of the entity class may be final.
	
	If an entity instance is to be passed by value as a detached object (e.g., through a remote interface), the entity class must implement the Serializable interface.
	Entities support inheritance, polymorphic associations, and polymorphic queries.
	Both abstract and concrete classes can be entities. Entities may extend non-entity classes as well as entity classes, and non-entity classes may extend entity classes.
	The persistent state of an entity is represented by instance variables, which may correspond to Java- Beans properties. An instance variable must be directly accessed only from within the methods of the entity by the entity instance itself. Instance variables must not be accessed by clients of the entity. The state of the entity is available to clients only through the entity’s methods—i.e., accessor methods (get- ter/setter methods) or other business methods.



p. En résumé:
- votre Entité peut soit utiliser une annotation, soit être configurée via XML
- elle doit avoir un constructeur public ou protected sans argument
- la classe doit être une « top-level class », il n’est pas possible d’utiliser un enum ou une interface.
- la classe ne doit pas être finale
- nécessité d’implémenter l’interface Serializable si l’object doit être détaché

bc.. 
package org.letouilleur.sample;
 
 
import javax.persistence.*;
import java.io.Serializable;
 
/**
 * Instrument is a simple JPA for http://touilleur-express.fr
 *
 * @author Nicolas Martignole
 * @since 5 août 2010 21:47:50
 */
@Entity
public class Instrument implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
 
    @Column(nullable = false,length = 12)
    private String isin;
    @Column(nullable = true,length = 150)
    private String description;
 
    // Required for JPA2
 
    protected Instrument() {
    }
 
    public Instrument(final String isin) {
        this(isin,null);
    }
 
    public Instrument(final String isin, final String description) {
        if (isin == null) {
            throw new NullPointerException("ISIN cannot be null");
        }
        this.isin = isin;
        this.description = description;
    }
 
 
    public Long getId() {
        return id;
    }
 
    public String getIsin() {
        return isin;
    }
 
 
    public String getDescription() {
        return description;
    }
 
}

p. Notez l’absence de setter. Il n’est pas nécessaire d’en déclarer car j’utilise (et je préfère) la déclaration des annotations sur les propriétés plutôt que sur les méthodes. Cela permet de regrouper au début de mon fichier l’ensemble des annotations, et de ne pas devoir chercher d’éventuelles contraintes

p. Ajoutons les méthodes toString, equals et hashCode. Etant donné que l’ISIN est une clé métier, je vais m’en servir pour gérer l’identité de mon Entité. Je modifie aussi le constructeur afin d’utiliser la classe Preconditions de Google Guava. Cela me permet d’alléger le test sur l’ISIN. Pensez-y lorsque vous avez une méthode avec 5 ou 6 arguments obligatoires. Guava est pratique et permet d’économiser du code.

p. Pour la partie equals et hashCode, je m’appuie sur l’ISIN qui est une clé métier. Cela m’arrange bien, pas besoin de gérer l’id qui peut être null tant que l’entité n’est pas persisté.

bc.. 
package org.letouilleur.sample;
 
import com.google.common.base.Objects;
 
import static com.google.common.base.Preconditions.*;
 
import javax.persistence.*;
import java.io.Serializable;
 
/**
 * Instrument is a simple JPA for http://touilleur-express.fr
 *
 * @author Nicolas Martignole
 * @since 5 août 2010 21:47:50
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"ID", "ISIN"}))
public class Instrument implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
 
    @Column(nullable = false, length = 12, name = "ISIN")
    private String isin;
    @Column(nullable = true, length = 150)
    private String description;
 
    // Required for JPA2
    protected Instrument() {
    }
 
    public Instrument(final String isin) {
        this(isin, null);
    }
 
    public Instrument(final String isin, String description) {
        checkNotNull(isin, "Isin cannot be null");   // Google Guava
        this.isin = isin;
        this.description = description;
    }
 
    public Long getId() {
        return id;
    }
 
    public String getIsin() {
        return isin;
    }
 
    public String getDescription() {
        return description;
    }
 
    @Override
    public String toString() {
       // Google Guava
        return Objects.toStringHelper(this).add("id", getId()).add("isin", getIsin()).add("description", getDescription()).toString();
    }
 
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instrument that = (Instrument) o;
        return Objects.equal(that.isin, isin);   // Google Guava
    }
 
    @Override
    public int hashCode() {
        return Objects.hashCode(isin); // Google Guava
    }
}

h2. Soyons fou

p. Prenons une autre approche un peu différente. Si je vous montre d’abord le code du test unitaire, réfléchissez sur l’implémentation de la classe InstrumentV2 afin qu’elle passe le test unitaire. Vous verrez, c’est un exercice intéressant.

bc.. 
package org.letouilleur.sample;
 
import junit.framework.TestCase;
 
import java.util.List;
 
public class InstrumentV2UnitTest extends TestCase {
 
    public void testShouldCreateAndFindAnEntity() {
 
        InstrumentV2 instrument = new InstrumentV2("FR123456");
        InstrumentV2 instrument2 = new InstrumentV2("US94394949");
 
        InstrumentV2.persist(instrument);
        InstrumentV2.persist(instrument2);
 
        List<InstrumentV2> results=InstrumentV2.findAll() ;
        assertEquals(2,results.size());
 
        assertEquals(instrument, InstrumentV2.find("FR123456"));
 
    }
 
}

p. A la lecture du code, nous voyons donc des méthodes statiques déclarées sur l’entité. A votre avis qu’est-ce que cela donne ? Et bien c’est faisable. Il y a plusieurs approches différentes, mais j’ai fait cela à ma manière, avec un bloc static pour créer le contexte de persistence. Je pense que l’annotation @PersistenceContexte ne servira à rien, mais je la laisse là.

bc.. 
package org.letouilleur.sample;
 
import com.google.common.base.Objects;
 
import javax.persistence.*;
import java.io.Serializable;
import java.sql.DriverManager;
import java.util.List;
 
import static com.google.common.base.Preconditions.checkNotNull;
 
/**
 * Instrument is a simple JPA for http://touilleur-express.fr
 *
 * @author Nicolas Martignole
 * @since 5 août 2010 21:47:50
 */
@Entity
@NamedQuery(name = "byIsin", query = "FROM InstrumentV2 I WHERE I.isin=:pisin")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"ID", "ISIN"}))
public class InstrumentV2 implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
 
    @Column(nullable = false, length = 12, name = "ISIN")
    private String isin;
    @Column(nullable = true, length = 150)
    private String description;
 
    @PersistenceContext
    static EntityManager em;
 
 
    static {
        System.out.println("--- Initializing context ---");
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            DriverManager.getConnection("jdbc:derby:memory:unit-testing-jpa;create=true").close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            EntityManagerFactory emFactory = Persistence.createEntityManagerFactory("testPU");
            em = emFactory.createEntityManager();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
 
    // Required for JPA2
 
    protected InstrumentV2() {
    }
 
    public InstrumentV2(final String isin) {
        this(isin, null);
    }
 
    public InstrumentV2(final String isin, String description) {
        checkNotNull(isin, "Isin cannot be null");
        this.isin = isin;
        this.description = description;
    }
 
    public Long getId() {
        return id;
    }
 
    public String getIsin() {
        return isin;
    }
 
 
    public String getDescription() {
        return description;
    }
 
    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", getId()).add("isin", getIsin()).add("description", getDescription()).toString();
    }
 
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstrumentV2 that = (InstrumentV2) o;
        return Objects.equal(that.isin, isin);
    }
 
    @Override
    public int hashCode() {
        return Objects.hashCode(isin);
    }
 
    public static InstrumentV2 find(String isin) {
        Query q = em.createNamedQuery("byIsin").setParameter("pisin", isin);
        return (InstrumentV2) q.getSingleResult();
    }
 
    public static void persist(InstrumentV2 instrument) {
        checkNotNull(instrument);
        em.getTransaction().begin();
        em.persist(instrument);
        em.getTransaction().commit();
    }
 
    @SuppressWarnings("unchecked")
    public static List<InstrumentV2> findAll(){
        return em.createQuery("from InstrumentV2").getResultList();
    }
}


p. Si vous voulez tester, n’oubliez pas d’ajouter la class InstrumentV2 dans la déclaration de persistence du fichier persistence.xml. Et vous verrez, cela marche très bien !

p. C’est une approche similaire, quoiqu’un peu plus compliquée, qu’utilise Play! Framework. Attention, Play utilise un système d’enrichissement du code à la compilation qui permet de donner une implémentation des méthodes type find, save, delete, mais certainement pas quelque chose avec un bout de code static comme je l’a fait ici.

p. Je rigole car vous vous êtes dit : « tiens il ne parle pas de Play!« . Et alors que Madame vous tartine le dos pendant que vous lisez ce billet sur votre iPad sur la plage, PAF au moment où vous ne vous y attendez pas, je vous reparle de Play… Avouez que je vous ai bien eu non ?

p. Allez, je vais vous laisser tranquille, passez de bonnes vacances.