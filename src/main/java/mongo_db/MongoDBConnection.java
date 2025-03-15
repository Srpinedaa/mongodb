package mongo_db;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.github.lalyos.jfiglet.FigletFont;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MongoDBConnection {

    public static String rojo = "\u001B[31m"; // Rojo
    public static String verde = "\u001B[32m"; // Verde
    public static String amarillo = "\u001B[33m"; // Amarillo
    public static String azul = "\u001B[34m"; // Azul
    public static String reset = "\u001B[0m"; // Reset

    public static void main(String[] args) {
        String uri = "mongodb://localhost:27017";

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("m6_MDB");

            // for (String name : database.listCollectionNames()) {
            // System.out.println("Colección encontrada: " + name);
            // }
            iniciarSesion(database);
            System.out.println("Conexión exitosa a MongoDB.");
        } catch (Exception e) {
            System.err.println("Error al conectar con MongoDB: " + e.getMessage());
        }
    }

    public static void iniciarSesion(MongoDatabase database) {
        try (Scanner lector = new Scanner(System.in)) {
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
                            pantallaAdministrador(database, lector);
                            break;
                        default:
                            break;
                    }
                } else {
                    System.out.println("Contraseña incorrecta.");
                    iniciarSesion(database);
                }
            } else {
                System.out.println("El nombre de usuario no existe.");
                iniciarSesion(database);
            }
        } catch (Exception e) {
            System.err.println("Error durante el inicio de sesión: " + e.getMessage());
        }
    }

    public static void pantallaHomeUsuario(MongoDatabase database, Scanner lector, String userId) {

        try {
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
                if (criticas != null) {
                    for (Document critica : criticas) {
                        System.out.println("- " + critica.getString("comentario") + " - " + critica.get("puntuacion"));
                    }
                } else {
                    System.out.println("No hay criticas.");
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
                System.out.println(actor.get("nacionalidad") + " - " + actor.get("fecha_nacimiento") + " - "
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
        } catch (Exception e) {
            System.err.println("Error en pantallaHomeUsuario: " + e.getMessage());
        }

    }

    public static void buscador(MongoDatabase database, Scanner lector, String userId) {
        try {
            Boolean peliculaEncontrada = false;
            int peliculaId = 0;
            System.out.println("Volver al menu (s/n)");
            if (lector.nextLine().toLowerCase().equals("s")) {
                pantallaHomeUsuario(database, lector, userId);
            }

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
                if (criticas != null) {
                    for (Document critica : criticas) {
                        System.out.println("- " + critica.getString("comentario") + " - " + critica.get("puntuacion"));
                    }
                } else {
                    System.out.println("No hay criticas.");
                }
            } else {
                peliculaEncontrada = false;
                System.out.println("pelicula no encontrada.");
                buscador(database, lector, userId);
            }
            if (peliculaEncontrada) {
                System.out.println("1. agregar pelicula a favoritas \n 2. Comentar pelicula \n 3. Volver al menu");
                int opcion = lector.nextInt();
                lector.nextLine();
                accionesPelicula(opcion, peliculaId, userId, database, lector, collectionPelicula);
            }
        } catch (Exception e) {
            System.err.println("Error en buscador: " + e.getMessage());
        }
    }

    public static void accionesPelicula(int opcion, int peliculaId, String userId, MongoDatabase database,
            Scanner lector, MongoCollection<Document> collectionPelicula) {
        try {
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
        } catch (Exception e) {
            System.err.println("Error en accionesPelicula: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static void perfilUsuario(MongoDatabase database, Scanner lector, String userId) {
        try {
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
                if (criticas != null) {
                    for (Document critica : criticas) {
                        if (critica.get("usuario") != null) {
                            if (critica.get("usuario").equals(userId)) {
                                System.out.println(
                                        "- " + critica.getString("comentario") + " - " + critica.get("puntuacion"));
                            }
                        }

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
                    cambiarPass(lector, database, userId);
                    break;
                case 2:
                    pantallaHomeUsuario(database, lector, userId);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error en perfilUsuario: " + e.getMessage());
        }
    }

    public static void cambiarPass(Scanner lector, MongoDatabase database, String userId) {
        MongoCollection<Document> collectionUsuarios = database.getCollection("usuarios");

        System.out.println("Introduce tu nueva contraseña:");
        String password = lector.nextLine();
        System.out.println("Vuelve a introducir tu contraseña:");
        String password2 = lector.nextLine();
        if (password.equals(password2)) {
            Document filtro = new Document("_id", userId);
            Document actualizacion = new Document("$set", new Document("password", password2));
            collectionUsuarios.updateOne(filtro, actualizacion);
            System.out.println("Documents actualitzats correctament!");
            perfilUsuario(database, lector, userId);
        } else {
            System.out.println("Las contraseñas no coinciden.");
            cambiarPass(lector, database, userId);
        }
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static void pantallaAdministrador(MongoDatabase database, Scanner lector) {

        try {
            System.out.println(rojo + "Bienvenido" + reset);
            System.out.println(
                    "1: Administrar peliculas. \n 2: Administrar actores. \n 3: Administrar usuarios. \n 4: Cerrar sesion");
            int opcion = lector.nextInt();
            lector.nextLine();
            switch (opcion) {
                case 1:
                    administrarPeliculas(database, lector);
                    break;
                case 2:
                    administrarActores(database, lector);
                    break;
                case 3:
                    administrarUsuarios(database, lector);
                    break;
                default:
                case 4:
                    System.out.println("Sesion cerrada.");
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error en pantallaAdministrador: " + e.getMessage());
        }
    }

    public static void administrarPeliculas(MongoDatabase database, Scanner lector) {
        try {
            MongoCollection<Document> collection = database.getCollection("peliculas");

            System.out.println(
                    "1: Insertar Pelicula \n 2: Leer Pelicula \n 3: Actualizar Pelicula \n 4: Eliminar Pelicula \n 5: Volver al menu");
            switch (lector.nextInt()) {
                case 1:
                    lector.nextLine();
                    insertarPelicula(database, lector, collection);
                    break;
                case 2:
                    lector.nextLine();
                    mostrarPelicula(database, lector, collection);
                    break;
                case 3:
                    lector.nextLine();
                    actualizarPelicula(database, lector, collection);
                    break;
                case 4:
                    lector.nextLine();
                    eliminarPelicula(lector, collection);
                    break;
                case 5:
                    lector.nextLine();
                    pantallaAdministrador(database, lector);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error en administrarPeliculas: " + e.getMessage());
        }
    }

    public static void insertarPelicula(MongoDatabase database, Scanner lector,
            MongoCollection<Document> collectionPelicula) {
        try {
            System.out.println("Introduce el id de la película:");
            int idPelicula = lector.nextInt();
            lector.nextLine(); // Limpiar buffer

            Document peliculaEncontrada = collectionPelicula.find(eq("_id", idPelicula)).first();
            if (peliculaEncontrada != null) {
                System.out.println("Ya existe la película con el id: " + idPelicula);
                insertarPelicula(database, lector, collectionPelicula);
            }

            System.out.println("Introduce el título de la película:");
            String titulo = lector.nextLine();
            System.out.println("Introduce el género principal de la película:");
            String generoPrincipal = lector.nextLine().toLowerCase();
            System.out.println("Introduce el genero secundario de la pelicula:");
            String generoSecundario = lector.nextLine().toLowerCase();
            List<String> generos = Arrays.asList(generoPrincipal, generoSecundario);
            System.out.println("Introduce el año de la película:");
            int anyo = lector.nextInt();
            lector.nextLine(); // Limpiar buffer

            // Pedir nombres de actores
            System.out.println("Introduce el actor principal de la película:");
            String actorPrincipal = lector.nextLine();
            System.out.println("Introduce el actor secundario de la película:");
            String actorSecundario = lector.nextLine();
            System.out.println("Introduce el actor de reparto de la película:");
            String actorReparto = lector.nextLine();

            // Obtener la colección de actores
            MongoCollection<Document> collectionActores = database.getCollection("actores");

            // Buscar los actores en la base de datos
            Document actorPrincipalDoc = collectionActores.find(eq("nombre", actorPrincipal)).first();
            Document actorSecundarioDoc = collectionActores.find(eq("nombre", actorSecundario)).first();
            Document actorRepartoDoc = collectionActores.find(eq("nombre", actorReparto)).first();

            if (actorPrincipalDoc == null || actorSecundarioDoc == null || actorRepartoDoc == null) {
                System.out.println("Uno o más actores no existen en la base de datos.");
                return; // No insertamos la película si faltan actores
            }

            // Crear un array con los IDs de los actores
            List<String> idsActores = Arrays.asList(
                    actorPrincipalDoc.get("_id").toString(),
                    actorSecundarioDoc.get("_id").toString(),
                    actorRepartoDoc.get("_id").toString());

            // Crear el documento de la película
            Document documentoFilm = new Document("_id", idPelicula)
                    .append("titulo", titulo.toLowerCase())
                    .append("genero", generos)
                    .append("anyo", anyo)
                    .append("actores", idsActores); // Insertamos el array correctamente

            collectionPelicula.insertOne(documentoFilm);

            // Buscar la película recién insertada
            Document pelicula = collectionPelicula.find(eq("titulo", titulo.toLowerCase())).first();
            if (pelicula == null) {
                System.out.println("Error al insertar la película.");
                return;
            }

            // Pedir datos del director
            System.out.println("Introduce el nombre del director:");
            String nombreDirector = lector.nextLine();
            System.out.println("Introduce la nacionalidad del director:");
            String nacionalidad = lector.nextLine();
            System.out.println("Introduce la fecha de nacimiento del director:");
            String fechaNacimiento = lector.nextLine();
            System.out.println("Introduce la biografía del director:");
            String biografia = lector.nextLine();

            // Crear el documento del director
            Document director = new Document("nombre", nombreDirector)
                    .append("nacionalidad", nacionalidad)
                    .append("fecha_nacimiento", fechaNacimiento)
                    .append("biografia", biografia);

            // Actualizar la película agregando el director sin duplicados
            collectionPelicula.updateOne(
                    eq("_id", idPelicula),
                    new Document("$addToSet", new Document("director", director)));

            System.out.println("Película insertada correctamente.");
            pantallaAdministrador(database, lector);
        } catch (Exception e) {
            System.out.println("Error al insertar la película: " + e.getMessage());
        }
    }

    public static void mostrarPelicula(MongoDatabase database, Scanner lector,
            MongoCollection<Document> collectionPelicula) {
        try {
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
                if (criticas != null) {
                    for (Document critica : criticas) {
                        System.out.println("- " + critica.getString("comentario") + " - " + critica.get("puntuacion"));
                    }
                } else {
                    System.out.println("No hay criticas.");
                }
            }
            System.out.println("¿Deseas buscar una pelicula en específico? (s/n)");
            String respuesta = lector.nextLine();
            if (respuesta.toLowerCase().equals("s")) {
                System.out.println("Introduce el titulo de la pelicula a buscar:");
                String titulo = lector.nextLine();
                Document pelicula = collectionPelicula.find(eq("titulo", titulo)).first();
                mostrarPelicula(database, lector, collectionPelicula);
                if (pelicula != null) {
                    System.out.println("Pelicula encontrada: " + pelicula.toJson());
                } else {
                    System.out.println("Pelicula no encontrada.");
                }
            } else {
                administrarPeliculas(database, lector);
            }
        } catch (Exception e) {
            System.err.println("Error en mostrarPelicula: " + e.getMessage());
        }

    }

    @SuppressWarnings("unused")
    public static void actualizarPelicula(MongoDatabase database, Scanner lector,
            MongoCollection<Document> collectionPelicula) {

        try {
            System.out.println("Introduce el titulo de la pelicula que quieres actualizar:");
            String titulo = lector.nextLine();
            Document pelicula = collectionPelicula.find(eq("titulo", titulo)).first();
            int peliculaId = Integer.parseInt(pelicula.get("_id").toString());

            if (pelicula != null) {
                System.out.println("pelicula encontrada: " + pelicula.toJson());
                System.out.println("Introduce el nombre del premio:");
                String nombrePremio = lector.nextLine();
                System.out.println("introduce la categoria en la que gano");
                String categoria = lector.nextLine();
                System.out.println("Introduce el año en el que gano la pelicula:");
                int anyo = lector.nextInt();
                lector.nextLine(); // Limpiar buffer
                Document premios = new Document("nombre", nombrePremio).append("categoria", categoria).append("año",
                        anyo);
                Document filtroPelicula = new Document("_id", peliculaId);
                Document actualizacionPelicula = new Document("$addToSet", new Document("premios", premios));
                collectionPelicula.updateOne(filtroPelicula, actualizacionPelicula);
                System.out.println("Documents actualitzats correctament!");
            } else {
                System.out.println("pelicula no encontrada.");
            }
        } catch (Exception e) {
            System.err.println("Error en actualizarPelicula: " + e.getMessage());
        }
        System.out.println("Inserta los nuevos premios de la pelicula:");

    }

    public static void eliminarPelicula(Scanner lector, MongoCollection<Document> collectionPelicula) {
        try {
            System.out.println("Introduce el titulo de la pelicula que quieres eliminar:");
            String titulo = lector.nextLine();
            Document pelicula = collectionPelicula.find(eq("titulo", titulo)).first();
            if (pelicula != null) {
                System.out.println("pelicula encontrada: " + pelicula.toJson());
                System.out.println("Estas seguro de que quieres eliminar la pelicula? (s/n)");
                String respuesta = lector.nextLine();
                if (respuesta.toLowerCase().equals("s")) {
                    collectionPelicula.deleteOne(eq("titulo", titulo));
                    System.out.println("Pelicula eliminada correctamente.");
                } else {
                    System.out.println("Operacion cancelada.");
                }
            } else {
                System.out.println("pelicula no encontrada.");
            }
        } catch (Exception e) {
            System.err.println("Error en eliminarPelicula: " + e.getMessage());
        }
    }

    public static void administrarActores(MongoDatabase database, Scanner lector) {

        try {
            MongoCollection<Document> collection = database.getCollection("actores");
            System.out.println(
                    "1: Insertar actor \n 2: Leer actor \n 3: Actualizar actor \n 4: Eliminar actor \n 5: Volver al menu");
            switch (lector.nextInt()) {
                case 1:
                    insertarActor(database, lector, collection);
                    break;
                case 2:
                    mostrarActor(database, lector, collection);
                    break;
                case 3:
                    actualizarActor(database, lector, collection);
                    break;
                case 4:
                    eliminarActor(database, lector, collection);
                    break;
                default:
                    pantallaAdministrador(database, lector);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error en administrarActores: " + e.getMessage());
        }

    }

    private static void insertarActor(MongoDatabase database, Scanner lector,
            MongoCollection<Document> collectionActor) {
        try {
            MongoCollection<Document> collectionPelicula = database.getCollection("peliculas");

            lector.nextLine();
            System.out.println("Introduce el identificador del actor:");
            String idActor = lector.nextLine();
            System.out.println("Introduce el nombre del actor:");
            String nombre = lector.nextLine();
            System.out.println("Introduce la nacionalidad del actor:");
            String nacionalidad = lector.nextLine();
            System.out.println("Introduce la fecha de nacimiento del actor:");
            String fechaNacimiento = lector.nextLine();
            System.out.println("Introduce la biografía del actor:");
            String biografia = lector.nextLine();

            Document documentoActor = new Document("nombre", nombre.toLowerCase())
                    .append("nacionalidad", nacionalidad.toLowerCase())
                    .append("fecha_nacimiento", fechaNacimiento)
                    .append("biografia", biografia).append("_id", idActor);
            collectionActor.insertOne(documentoActor);
            Document actor = collectionActor.find(eq("nombre", nombre)).first();

            if (actor != null) {
                System.out.println("Entra");
                String actorId = actor.get("_id").toString();
                System.out.println("Introduce la pelicula en las que ha trabajado:");
                String tituloPelicula = lector.nextLine();
                Document pelicula = collectionPelicula.find(eq("titulo", tituloPelicula)).first();
                if (pelicula != null) {
                    Document filtroPelicula = new Document("_id", pelicula.get("_id"));
                    Document actualizacionActor = new Document("$addToSet",
                            new Document("actores_id", actorId));
                    collectionPelicula.updateOne(filtroPelicula, actualizacionActor);
                    System.out.println("Pelicula insertada correctamente.");
                } else {
                    System.out.println("La pelicula no existe.");
                }

            } else {
                System.out.println("Error al insertar el actor.");
            }
            System.out.println("Actor insertado correctamente.");
        } catch (Exception e) {
            System.err.println("Error en insertarActor: " + e.getMessage());
        }
    }

    private static void mostrarActor(MongoDatabase database, Scanner lector, MongoCollection<Document> collection) {
        try {
            System.out.println("A C T O R E S: ");
            for (Document actor : collection.find()) {
                try {
                    String nombre = capitalize(actor.get("nombre").toString());
                    String asciiArt = FigletFont.convertOneLine(nombre);
                    System.out.println(asciiArt);
                } catch (Exception e) {
                    System.err.println("Error al generar ASCII Art: " + e.getMessage());
                }
                System.out.println(
                        actor.get("nacionalidad") + " - " + actor.get("fecha_nacimiento") + " - "
                                + actor.get("biografia"));
            }
            System.out.println("¿Deseas buscar un actor en específico? (s/n)");
            String respuesta = lector.nextLine();
            if (respuesta.toLowerCase().equals("s")) {
                System.out.println("Introduce el nombre del actor a buscar:");
                String nombre = lector.nextLine();
                Document actor = collection.find(eq("nombre", nombre.toLowerCase())).first();
                if (actor != null) {
                    System.out.println("Actor encontrado: " + actor.toJson());
                } else {
                    System.out.println("Actor no encontrado.");
                }
            } else {
                administrarActores(database, lector);
            }
        } catch (Exception e) {
            System.err.println("Error en mostrarActor: " + e.getMessage());
        }
    }

    private static void actualizarActor(MongoDatabase database, Scanner lector, MongoCollection<Document> collection) {
        lector.nextLine();
        try {
            MongoCollection<Document> collectionPelicula = database.getCollection("peliculas");

            System.out.println("Introduce el nombre del actor que quieres actualizar:");
            String nombre = lector.nextLine();
            Document actor = collection.find(eq("nombre", nombre.toLowerCase())).first();
            Document filtroActor = new Document();
            Document actualizacionActor = new Document();
            System.out.println("1: premios \n 2:peliculas");
            if (actor != null) {

                switch (lector.nextInt()) {
                    case 1:
                        lector.nextLine(); // Limpiar buffer
                        System.out.println("Introduce el nombre del premio:");
                        String nombrePremio = lector.nextLine();
                        System.out.println("introduce la categoria en la que gano");
                        String categoria = lector.nextLine();
                        System.out.println("Introduce el año en el que gano el premio:");
                        int anyo = lector.nextInt();
                        lector.nextLine(); // Limpiar buffer
                        Document premios = new Document("nombre", nombrePremio).append("categoria", categoria).append(
                                "año",
                                anyo);
                        filtroActor = new Document("_id", actor.get("_id"));
                        actualizacionActor = new Document("$addToSet", new Document("premios", premios));
                        break;
                    case 2:
                        lector.nextLine(); // Limpiar buffer
                        System.out.println("Introduce la pelicula en la que ha trabajado:");
                        String tituloPelicula = lector.nextLine();
                        Document pelicula = collectionPelicula.find(eq("titulo", tituloPelicula)).first();
                        if (pelicula != null) {
                            filtroActor = new Document("_id", actor.get("_id"));
                            actualizacionActor = new Document("$addToSet",
                                    new Document("peliculas", pelicula.get("_id")));
                            System.out.println("Pelicula insertada correctamente.");
                        } else {
                            System.out.println("La pelicula no existe.");
                        }
                        break;
                    default:
                        break;
                }
                collection.updateOne(filtroActor, actualizacionActor);
                System.out.println("Documents actualitzats correctament!");
            } else {
                System.out.println("Actor no encontrado.");
            }
        } catch (Exception e) {
            System.err.println("Error en actualizarActor: " + e.getMessage());
        }
        pantallaAdministrador(database, lector);
    }

    @SuppressWarnings("unused")
    private static void eliminarActor(MongoDatabase database, Scanner lector, MongoCollection<Document> collection) {
        lector.nextLine();
        try {
            System.out.println("Introduce el nombre del actor que quieres eliminar:");
            String nombre = lector.nextLine();
            Document actor = collection.find(eq("nombre", nombre.toLowerCase())).first();
            String actorId = actor.get("_id").toString();

            if (actor != null) {
                // System.out.println("Actor encontrado: " + actor.toJson());
                try {
                    String nombreActor = capitalize(actor.get("nombre").toString());
                    String asciiArt = FigletFont.convertOneLine(nombreActor);
                    System.out.println(asciiArt);
                } catch (Exception e) {
                    System.err.println("Error al generar ASCII Art: " + e.getMessage());
                }
                System.out.println(
                        actor.get("nacionalidad") + " - " + actor.get("fecha_nacimiento") + " - "
                                + actor.get("biografia"));
                System.out.println("¿Estás seguro de que quieres eliminar el actor? (s/n)");
                String respuesta = lector.nextLine();
                if (respuesta.toLowerCase().equals("s")) {
                    collection.deleteOne(eq("nombre", nombre.toLowerCase()));
                    Document filtroPelicula = new Document("actores_id", actorId);
                    Document actualizacionPelicula = new Document("$pull", new Document("actores_id", actorId));
                    database.getCollection("peliculas").updateOne(filtroPelicula, actualizacionPelicula);
                    System.out.println("Actor eliminado correctamente.");
                } else {
                    System.out.println("Operación cancelada.");
                }
            } else {
                System.out.println("Actor no encontrado.");
            }
        } catch (Exception e) {
            System.err.println("Error en eliminarActor: " + e.getMessage());
        }
        pantallaAdministrador(database, lector);

    }

    public static void administrarUsuarios(MongoDatabase database, Scanner lector) {
        try {
            MongoCollection<Document> collection = database.getCollection("usuarios");

            System.out.println("1 Leer usuario  \n 2: Eliminar usuario \n 3: Volver al menu");
            switch (lector.nextInt()) {
                case 1:
                    mostrarUsuario(database, lector, collection);
                    break;
                case 2:
                    eliminarUsuario(lector, collection);
                    break;
                default:
                case 3:
                    pantallaAdministrador(database, lector);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error en administrarUsuarios: " + e.getMessage());
        }
    }

    private static void mostrarUsuario(MongoDatabase database, Scanner lector, MongoCollection<Document> collection) {
        lector.nextLine();
        try {
            System.out.println("U S U A R I O S: ");
            for (Document usuario : collection.find()) {
                try {
                    String nombre = capitalize(usuario.get("nombre").toString());
                    String asciiArt = FigletFont.convertOneLine(nombre);
                    System.out.println(asciiArt);
                } catch (Exception e) {
                    System.err.println("Error al generar ASCII Art: " + e.getMessage());
                }
                System.out.println(
                        usuario.get("email") + " - " + usuario.get("rol") + " - "
                                + usuario.get("fecha_registro"));
            }

            System.out.println("¿Deseas buscar un usuario en específico? (s/n)");
            String respuesta = lector.nextLine();
            if (respuesta.toLowerCase().equals("s")) {
                System.out.println("Introduce el nombre de usuario a buscar:");
                String nombreUsuario = lector.nextLine();
                Document usuario = collection.find(eq("_id", nombreUsuario.toLowerCase())).first();
                if (usuario != null) {
                    System.out.println("Usuario encontrado: " + usuario.toJson());
                } else {
                    System.out.println("Usuario no encontrado.");
                }
                administrarUsuarios(database, lector);

            } else {
                administrarUsuarios(database, lector);
            }
        } catch (Exception e) {
            System.err.println("Error en mostrarUsuario: " + e.getMessage());
        }
    }

    private static void eliminarUsuario(Scanner lector, MongoCollection<Document> collection) {
        lector.nextLine();
        try {
            System.out.println("Introduce el nombre de usuario que quieres eliminar:");
            String nombreUsuario = lector.nextLine();
            Document usuario = collection.find(eq("_id", nombreUsuario.toLowerCase())).first();
            if (usuario != null) {
                // System.out.println("Usuario encontrado: " + usuario.toJson());
                try {
                    String nombre = capitalize(usuario.get("nombre").toString());
                    String asciiArt = FigletFont.convertOneLine(nombre);
                    System.out.println(asciiArt);
                } catch (Exception e) {
                    System.err.println("Error al generar ASCII Art: " + e.getMessage());
                }
                System.out.println(
                        usuario.get("email") + " - " + usuario.get("rol") + " - "
                                + usuario.get("fecha_registro"));
                System.out.println("¿Estás seguro de que quieres eliminar el usuario? (s/n)");
                String respuesta = lector.nextLine();
                if (respuesta.toLowerCase().equals("s")) {
                    collection.deleteOne(eq("_id", nombreUsuario.toLowerCase()));
                    System.out.println("Usuario eliminado correctamente.");
                } else {
                    System.out.println("Operación cancelada.");
                }
            } else {
                System.out.println("Usuario no encontrado.");
            }
        } catch (Exception e) {
            System.err.println("Error en eliminarUsuario: " + e.getMessage());
        }
    }
}