package mongo_db;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.github.lalyos.jfiglet.FigletFont;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;

import java.util.List;
import java.util.Scanner;

public class MongoDBConnection {
    public static void main(String[] args) {
        String uri = "mongodb://localhost:27017";

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("m6_MDB");

            for (String name : database.listCollectionNames()) {
                System.out.println("Colección encontrada: " + name);
            }
            iniciarSesion(database);
            System.out.println("Conexión exitosa a MongoDB.");
        } catch (Exception e) {
            System.err.println("Error al conectar con MongoDB: " + e.getMessage());
        }
    }

    public static void iniciarSesion(MongoDatabase database) {
        Scanner lector = new Scanner(System.in);
        MongoCollection<Document> collection = database.getCollection("usuarios");
        System.out.println("Indique su nombre de usuario:");
        String nombreUser = lector.nextLine();
        Document user = collection.find(eq("_id", nombreUser.toLowerCase())).first();

        if (user != null) {
            System.out.println("Indique su contraseña:");
            String password = lector.nextLine();
            if (user.get("password").equals(password)) {
                switch (user.get("rol").toString().toLowerCase()) {
                    case "usuario":
                        pantallaHomeUsuario(database, lector, user.get("_id").toString());
                        break;
                    case "admin":
                        pantallaAdministrador(database, lector, user.get("_id").toString());
                        break;
                    default:
                        break;
                }
            } else {
                System.out.println("Contraseña incorrecta.");
            }
        } else {
            System.out.println("El nombre de usuario no existe.");
            iniciarSesion(database);
        }
        lector.close();
    }

    public static void pantallaHomeUsuario(MongoDatabase database, Scanner lector, String userId) {

        MongoCollection<Document> collectionPelicula = database.getCollection("peliculas");
        MongoCollection<Document> collectionActores = database.getCollection("actores");
        System.out.println("P E L I C U L A S: ");
        for (Document pelicula : collectionPelicula.find()) {
            try {
                String titulo = capitalize(pelicula.get("titulo").toString());
                String asciiArt = FigletFont.convertOneLine(titulo);
                System.out.println(asciiArt);
            } catch (Exception e) {
                System.err.println("Error al generar ASCII Art: " + e.getMessage());
            }
            System.out.println(pelicula.get("genero") + " - " + pelicula.get("calificacion"));
            @SuppressWarnings("unchecked")
            List<Document> criticas = (List<Document>) pelicula.get("criticas");
            for (Document critica : criticas) {
                System.out.println("- " + critica.getString("comentario") + " - " + critica.get("puntuacion"));
            }
        }
        System.out.println("A C T O R E S: ");
        for (Document actor : collectionActores.find()) {
            try {
                String nombre = capitalize(actor.get("nombre").toString());
                String asciiArt = FigletFont.convertOneLine(nombre);
                System.out.println(asciiArt);
            } catch (Exception e) {
                System.err.println("Error al generar ASCII Art: " + e.getMessage());
            }
            System.out
                    .println(actor.get("nacionalidad") + " - " + actor.get("fecha_nacimiento") + " - "
                            + actor.get("biografia"));
        }
        System.out.println("Opcion Menu: \n 1:Home \n 2:Buscador \n 3:perfil \n 4: cerrar sesion");
        int opcion = lector.nextInt();
        lector.nextLine();

        switch (opcion) {
            case 1:
                pantallaHomeUsuario(database, lector, userId);
                break;
            case 2:
                buscador(database, lector, userId);
                break;
            case 3:
                perfilUsuario(database, lector, userId);
                break;
            case 4:
                System.out.println("Sesion cerrada.");
                break;
            default:
                break;
        }

    }

    public static void pantallaAdministrador(MongoDatabase database, Scanner lector, String userId) {
        String rojo = "\u001B[31m"; // Rojo
        String verde = "\u001B[32m"; // Verde
        String amarillo = "\u001B[33m"; // Amarillo
        String azul = "\u001B[34m"; // Azul
        String reset = "\u001B[0m"; // Reset
        System.out.println(rojo + "Bienvenido" + reset);
        System.out.println("1: Administrar peliculas. \n 2: Administrar actores. \n 3: Administrar usuarios.");
        int opcion = lector.nextInt();
        lector.nextLine();
        switch (opcion) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            default:
                break;
        }
    }

    public static void buscador(MongoDatabase database, Scanner lector, String userId) {
        Boolean peliculaEncontrada = false;
        int peliculaId = 0;
        MongoCollection<Document> collectionPelicula = database.getCollection("peliculas");
        System.out.println("Introduce el titulo de la pelicula a buscar:");
        String titulo = lector.nextLine();
        Document pelicula = collectionPelicula.find(eq("titulo", titulo.toLowerCase())).first();
        if (pelicula != null) {
            peliculaEncontrada = true;
            peliculaId = Integer.parseInt(pelicula.get("_id").toString());
            try {
                String asciiArt = FigletFont.convertOneLine(capitalize(pelicula.get("titulo").toString()));
                System.out.println(asciiArt);
            } catch (Exception e) {
                System.err.println("Error al generar ASCII Art: " + e.getMessage());
            }
            System.out.println(pelicula.get("genero") + " - " + pelicula.get("calificacion"));
            @SuppressWarnings("unchecked")
            List<Document> criticas = (List<Document>) pelicula.get("criticas");
            for (Document critica : criticas) {
                System.out.println("- " + critica.getString("comentario") + " - " + critica.get("puntuacion"));
            }
        } else {
            peliculaEncontrada = false;
            System.out.println("pelicula no trobat.");
        }
        if (peliculaEncontrada) {
            System.out.println("1. agregar pelicula a favoritas \n 2. Comentar pelicula \n 3. Volver al menu");
            int opcion = lector.nextInt();
            lector.nextLine();
            accionesPelicula(opcion, peliculaId, userId, database, lector, collectionPelicula);
        }
    }

    public static void accionesPelicula(int opcion, int peliculaId, String userId, MongoDatabase database,
            Scanner lector, MongoCollection<Document> collectionPelicula) {
        switch (opcion) {
            case 1:
                MongoCollection<Document> collectionUsuarios = database.getCollection("usuarios");
                Document filtro = new Document("_id", userId);

                Document actualizacion = new Document("$addToSet", new Document("peliculas_favoritas", peliculaId));

                collectionUsuarios.updateOne(filtro, actualizacion);

                System.out.println("Documents actualitzats correctament!");
                buscador(database, lector, userId);

                break;
            case 2:
                System.out.println("Introduce tu comentario:");
                String comentario = lector.nextLine();
                System.out.println("Introduce tu puntuacion:");
                int puntuacion = lector.nextInt();
                Document critica = new Document("comentario", comentario).append("puntuacion", puntuacion);
                Document filtroPelicula = new Document("_id", peliculaId);
                Document actualizacionPelicula = new Document("$addToSet", new Document("criticas", critica));
                collectionPelicula.updateOne(filtroPelicula, actualizacionPelicula);
                System.out.println("Documents actualitzats correctament!");
                buscador(database, lector, userId);
                break;
            case 3:
                pantallaHomeUsuario(database, lector, userId);
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    public static void perfilUsuario(MongoDatabase database, Scanner lector, String userId) {
        MongoCollection<Document> collectionUsuarios = database.getCollection("usuarios");
        Document user = collectionUsuarios.find(eq("_id", userId)).first();

        try {
            String asciiArt = FigletFont.convertOneLine(capitalize(user.get("_id").toString()));
            System.out.println(asciiArt);
        } catch (Exception e) {
            System.err.println("Error al generar ASCII Art: " + e.getMessage());
        }
        System.out.println("Peliculas favoritas: ");
        List<Integer> peliculasFavoritas = (List<Integer>) user.get("peliculas_favoritas");
        for (int pelicula_id : peliculasFavoritas) {
            Document pelicula = database.getCollection("peliculas").find(eq("_id", pelicula_id)).first();
            try {
                String asciiArt = FigletFont.convertOneLine(capitalize(pelicula.get("titulo").toString()));
                System.out.println(asciiArt);
            } catch (Exception e) {
                System.err.println("Error al generar ASCII Art: " + e.getMessage());
            }

            List<Document> criticas = (List<Document>) pelicula.get("criticas");
            for (Document critica : criticas) {
                if (critica.get("usuario").equals(userId)) {
                    System.out.println("- " + critica.getString("comentario") + " - " + critica.get("puntuacion"));
                }
            }
            try {
                String separador = FigletFont.convertOneLine("---------");
                System.out.println(separador);
            } catch (Exception e) {
                System.err.println("Error al generar ASCII Art: " + e.getMessage());
            }

        }
        System.out.println("1. modificar cuenta \n 2. Volver al menu");
        int opcion = lector.nextInt();
        lector.nextLine();
        switch (opcion) {
            case 1:
                System.out.println("Introduce tu nueva contraseña:");
                String password = lector.nextLine();
                Document filtro = new Document("_id", userId);
                Document actualizacion = new Document("$set", new Document("password", password));
                collectionUsuarios.updateOne(filtro, actualizacion);
                System.out.println("Documents actualitzats correctament!");
                perfilUsuario(database, lector, userId);
                break;
            case 2:
                pantallaHomeUsuario(database, lector, userId);
                break;
            default:
                break;
        }
    }

    public static void insertar(MongoDatabase database) {
        MongoCollection<Document> collection = database.getCollection("peliculas");

        Document documentoFilm = new Document("titulo", "Django")
                .append("genero", "Oeste")
                .append("puntuacion", 9);

        collection.insertOne(documentoFilm);
        System.out.println("Documento insertado correctamente.");
    }

    public static void leer(MongoDatabase database) {
        MongoCollection<Document> collection = database.getCollection("peliculas");

        Document user = collection.find(eq("titulo", "Django")).first();
        if (user != null) {
            System.out.println("pelicula trobat: " + user.toJson());
        } else {
            System.out.println("pelicula no trobat.");
        }

    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}