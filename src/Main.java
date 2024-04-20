import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import org.basex.examples.api.BaseXClient;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.*;
import java.util.Scanner;

public class Main {
    private static Scanner sc;
    private static final MongoClientURI uri = new MongoClientURI("mongodb://localhost:27017");
    private static final MongoClient client = new MongoClient(uri);
    private static MongoDatabase database = client.getDatabase("tienda");

    private static final MongoCollection<Document> collection_clientes = database.getCollection("clientes");
    private static final MongoCollection<Document> collection_pedidos = database.getCollection("pedidos");
    private static final MongoCollection<Document> collection_carrito = database.getCollection("carrito");

    public static void main(String[] args) {
        sc = new Scanner(System.in);
        //    menuXML();
        menuMongo();
    }

    //############ CONSULTAS MONGODB ########################
    public static void menuMongo() {

        ObjectId clientSelected = null;
        int opcion = -1;
        while (opcion != 0) {
            String mensaje = "1.Crear un nuevo cliente (no podrá haber email repetidos).\n" +
                    "2.Identificar cliente según el email. Dado el email se obtendrá el ID del cliente de forma que las siguientes consultas se harán sobre ese cliente. Para cambiar de cliente se tendrá que volver a seleccionar esta opción.\n" +
                    "3.Borrar un cliente.\n" +
                    "4.Modificar el valor de un campo de la información del cliente.\n" +
                    "5.Añadir producto al carrito del cliente. Se pedirá: id del producto y cantidad, así como si se desea seguir introduciendo más productos.\n" +
                    "6.Mostrar el carrito del cliente. Se mostrarán los datos del carrito y el precio total.\n" +
                    "7.Mostrar pedidos del cliente.\n" +
                    "8.Pagar el carrito de un cliente: se mostrará el carrito junto con una orden de confirmación. Si la orden es positiva se pasarán  todos los productos a formar parte de un nuevo pedido.\n" +
                    "9.Consulta 1: Teniendo en cuenta todos los clientes, calcular el total de la compra para cada carrito y listar los resultados ordenados por el total de forma ascendente. (No es necesario tener en cuenta la multiplicación de precio_unitario * cantidad con sumar los precio_unitario es suficiente).\n" +
                    "10.Consulta 2: Teniendo en cuenta todos los clientes, obtener el total gastado por cada cliente en todos sus pedidos.";

            opcion = pedirInt(mensaje);
            switch (opcion) {
                case 1:
                    addClient();
                    break;
                case 2:
                    clientSelected = getIdCliente(pedirString("Introduce el email del cliente a seleccionar"));
                    break;
                case 3:
                    if (clientSelected != null) {
                        deleteClient(clientSelected);
                    } else System.out.println("Debes seleccionar un cliente primero");
                    break;
                case 4:
                    updateClient(clientSelected);
                    break;
                case 5:
                    addProductToCart(clientSelected);
                    break;
                case 6:
                    viewCart(clientSelected);
                    break;
                case 7:
                    break;
                case 8:
                    break;
                case 9:
                    break;
                case 10:
                    break;
            }
        }
    }

    public static void viewCart(ObjectId client) {
        for (Object o : collection_carrito.find(new Document("_id", client))) {
            System.out.println(o.toString());
        }
    }

    public static void addProductToCart(ObjectId clientSelected) {
        String idProducto;
        int cantidad;
        do {
            idProducto = pedirString("Introduce el id del producto");
            cantidad = pedirInt("Introduce la cantidad a anadir");
            int disponibilidad = getDisponibilidad(idProducto);
            String nombreProducto = getProductName(idProducto);
            if (disponibilidad >= cantidad) {
                System.out.println(String.format("Hay %d  %s disponibles ", disponibilidad, nombreProducto));
                Double precio = getProductPrice(idProducto);
                Document productoEnCarrito = new Document("_id", clientSelected.toString())
                        .append("producto_id", idProducto)
                        .append("nombre", nombreProducto)
                        .append("cantidad", String.format("%d", cantidad))
                        .append("precio_unitario", String.format("%f", precio));
                collection_carrito.insertOne(productoEnCarrito);
            }

        } while (pedirString("Insertar otro producto al carro?(s/n)").equalsIgnoreCase("s"));

    }

    /**
     * Comprueba que el email no exista. Retornando el documento del cliente al que pertenece el email o null en caso de que no exista el email
     *
     * @param email
     * @return
     */
    public static Document emailValid(String email) {
        return collection_clientes.find(new Document("email", email)).first();
    }

    public static Document getClientById(ObjectId client) {
        return collection_clientes.find(new Document("_id", client)).first();
    }

    /**
     * Devuelve el id del cliente si existe, sino devuelve null
     *
     * @param email
     * @return
     */
    public static ObjectId getIdCliente(String email) {
        return emailValid(email).getObjectId("_id");
    }

    public static void updateClient(ObjectId id) {
        int contador = 0;
        Document client = getClientById(id);
        String keysClient = "";
        StringBuilder sb = new StringBuilder();
        for (String clave : client.keySet()) {
            if (contador > 0)
                sb.append(contador).append(".").append(clave).append("\n");
            contador++;
        }
        keysClient = sb.toString();
        int keyClient = pedirInt(keysClient);
        switch (keyClient) {
            case 1:
                client.put("nombre", pedirString("Introduce nuevo nombre"));
                break;
            case 2:
                client.put("email", pedirString("Introduce nuevo email"));
                break;
            case 3:
                client.put("direccion", pedirString("Introduce nueva direccion"));
                break;
            case 4:
                client.put("carrito", pedirString("Introduce nuevo carrito"));
                break;
            case 5:
                client.put("pedidos", pedirString("introduce nuevo pedido"));
                break;

        }
        collection_clientes.replaceOne(new Document("_id", client.getObjectId("_id")), client);
    }

    /**
     *
     */
    public static void addClient() {
        Document nuevoCliente = new Document();
        String nombre = pedirString("Nombre del cliente ");
        while (true) {
            String email = pedirString("Introduce el email");
            if (emailValid(email) == null) {
                String direccion = pedirString("Introduce la direccion");
                nuevoCliente.append("nombre", nombre);
                nuevoCliente.append("email", email);
                nuevoCliente.append("direccion", direccion);
                nuevoCliente.append("carrito", new Document());
                nuevoCliente.append("pedidos", new Document());
                collection_clientes.insertOne(nuevoCliente);
                System.out.println("Cliente creado correctamente");
                break;
            } else {
                System.out.println("El correo ya existe");
            }
        }
    }


    public static void deleteClient(ObjectId id) {
        DeleteResult dr = collection_clientes.deleteOne(new Document("_id", id));
        if (dr.getDeletedCount() > 0) {
            System.out.println("Eliminación correcta. " + dr.getDeletedCount() + " documentos eliminados.");
        } else {
            System.out.println("No se ha eliminado ningún documento");
        }
    }

    //############ CONSULTAS XML ########################
    public static void menuXML() {
        String mensaje = "1.Modificar el valor de un elemento de un XML según un ID.\n" +
                "2.Eliminar un producto según su ID.\n" +
                "3.Consulta 1: Obtener todos los productos por orden alfabético del nombre (se mostrarán los siguientes campos: id, nombre, precio, disponibilidad y categoria).\n" +
                "4.Consulta 2: Listar productos con una disponibilidad mayor a X unidades (se mostrarán los siguientes campos: id, nombre, precio, disponibilidad y categoria).\n" +
                "5.Consulta 3: Mostrar la categoría, el nombre y el precio del producto más caro para cada categoría. En el caso de haber varios se devolverá el de la primera posición.\n" +
                "6.Consulta 4: Mostrar el nombre de los productos y su fabricante para aquellos productos cuya descripción incluya una subcadena. Se deberá mostrar la información ordenada según el nombre del fabricante de forma inversa al alfabeto.\n" +
                "7.Consulta 5: Mostrar la cantidad total de productos en cada categoría (teniendo en cuenta el elemento disponibilidad) y calcular el porcentaje que representa respecto al total de productos.";
        int opcion = pedirInt(mensaje);
        switch (opcion) {
            case 1:
                updateById(pedirString("Introduce id del elemento a modificar"), pedirString("Introduce el nombre del campo a modificar"), pedirString("Introduce el nuevo valor"));
                break;
            case 2:
                deleteById(pedirString("Introduce el id del nodo a eliminar"));
                break;
            case 3:
                getProductsOrderedByName();
                break;
            case 4:
                getProductsByDisponibilidad(pedirInt("Introduce la disponibilidad minima"));
                break;
            case 5:
                getProductExpensive();
                break;
            case 6:
                getProductsByDescripcionCad(pedirString("Introduce cadena a buscar en la descripcion"));
                break;
            case 7:
                getCountbyCategoria();
                break;


        }


    }

    public static Double getProductPrice(String idProducto) {

        Double precio = 0.0;
        try (BaseXClient session = new BaseXClient("localhost", 1984, "admin", "abc123")) {
            String consulta = String.format("for $p in db:get('productos')//producto[id=%s] return data($p/precio)", idProducto);
            BaseXClient.Query query = session.query(consulta);
            query.bind("$nombre", "");
            while (query.more()) {
                precio = Double.parseDouble(query.next());
            }
            query.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return precio;
    }

    public static String getProductName(String idProducto) {
        String nombre = "";
        try (BaseXClient session = new BaseXClient("localhost", 1984, "admin", "abc123")) {
            String consulta = String.format("for $p in db:get('productos')//producto[id='%s'] return data($p/nombre)", idProducto);
            BaseXClient.Query query = session.query(consulta);
            query.bind("$disponibilidad", "");
            nombre = query.next();
            query.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return nombre;
    }

    public static int getDisponibilidad(String idProducto) {
        int cantidad = 0;
        try (BaseXClient session = new BaseXClient("localhost", 1984, "admin", "abc123")) {
            String consulta = String.format("for $p in db:get('productos')//producto[id='%s'] return data($p/disponibilidad)", idProducto);
            BaseXClient.Query query = session.query(consulta);
            query.bind("$disponibilidad", "");
            cantidad = Integer.parseInt(query.next());
            query.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cantidad;
    }

    /**
     * @param input
     */
    public static void consultaBaseX(String input) {
        try (BaseXClient session = new BaseXClient("localhost", 1984, "admin", "abc123")) {
            BaseXClient.Query query = session.query(input);
            while (query.more()) {
                System.out.println(query.next());
            }
            System.out.println(query.info());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * @param id
     * @param campo
     * @param nuevoValor
     */
    public static void updateById(String id, String campo, String nuevoValor) {
        try (BaseXClient session = new BaseXClient("localhost", 1984, "admin", "abc123")) {
            String consulta = String.format("let $p:= db:get('productos')//producto[id=%s] return replace value of node $p/%s with '%s' ", id, campo, nuevoValor);
            BaseXClient.Query query = session.query(consulta);
            System.out.println(query.more());

            System.out.println(query.info());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param id
     */
    public static void deleteById(String id) {
        try (BaseXClient session = new BaseXClient("localhost", 1984, "admin", "abc123")) {
            String consulta = String.format("delete node db:get('productos')//producto[id=%s]", id);
            BaseXClient.Query query = session.query(consulta);
            System.out.println(query.more());
            System.out.println(query.info());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /*for $p in db:get('productos')//producto
order by $p/nombre return concat('ID	',$p/id,' Nombre	',$p/nombre,' PRECIO	',$p/precio,' DISPONIBILIDAD	',$p/disponibilidad,' CATEGORIA	',$p/categoria)
*/

    /**
     *
     */
    public static void getProductsOrderedByName() {
        try (BaseXClient session = new BaseXClient("localhost", 1984, "admin", "abc123")) {
            String consulta = String.format("for $p in db:get('productos')//producto \n" +
                    "order by $p/nombre return concat('ID\t',$p/id,' Nombre\t',$p/nombre,' PRECIO\t',$p/precio,' DISPONIBILIDAD\t',$p/disponibilidad,' CATEGORIA\t',$p/categoria)");
            BaseXClient.Query query = session.query(consulta);
            while (query.more()) {

                System.out.println(query.next());
            }
            System.out.println(query.info());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * for $p in db:get('productos')//producto[number(disponibilidad) gt 14]
     * order by $p/nombre
     * return concat('ID	',$p/id,' Nombre	',$p/nombre,' PRECIO	',$p/precio,' DISPONIBILIDAD	',$p/disponibilidad,' CATEGORIA	',$p/categoria)
     *
     * @param disponibilidad
     */
    public static void getProductsByDisponibilidad(int disponibilidad) {
        try (BaseXClient session = new BaseXClient("localhost", 1984, "admin", "abc123")) {
            String consulta = String.format("for $p in db:get('productos')//producto[number(disponibilidad) gt %d]\n" +
                    "order by $p/nombre\n" +
                    "return concat('ID\t',$p/id,' Nombre\t',$p/nombre,' PRECIO\t',$p/precio,' DISPONIBILIDAD\t',$p/disponibilidad,' CATEGORIA\t',$p/categoria)", disponibilidad);
            BaseXClient.Query query = session.query(consulta);
            while (query.more()) {

                System.out.println(query.next());
            }
            System.out.println(query.info());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /*for $p in db:get('productos')//producto
    group by $categoria := $p/categoria
    let $precio:= max($p/precio)
    let $productos := $p[precio = $precio]
    let $producto := $productos[1]
    return concat(
      'Nombre	', $producto/nombre,'	Precio	', $producto/precio,'	Categoría	', $producto/categoria
    )*/
    public static void getProductExpensive() {
        try (BaseXClient session = new BaseXClient("localhost", 1984, "admin", "abc123")) {
            String consulta = String.format("for $p in db:get('productos')//producto\n" +
                    "    group by $categoria := $p/categoria\n" +
                    "    let $precio:= max($p/precio)\n" +
                    "    let $productos := $p[precio = $precio]\n" +
                    "    let $producto := $productos[1]\n" +
                    "    return concat(\n" +
                    "      'Nombre\t', $producto/nombre,'\tPrecio\t', $producto/precio,'\tCategoría\t', $producto/categoria\n" +
                    "    )");
            BaseXClient.Query query = session.query(consulta);
            while (query.more()) {

                System.out.println(query.next());
            }
            System.out.println(query.info());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * for $producto in db:get('productos')//producto
     * where contains($producto/descripcion,'de')
     * order by  $producto/fabricante descending
     * return concat(
     * 'Nombre	', $producto/nombre,'	Fabricante	',$producto/fabricante
     * )
     *
     * @param cadena
     */
    public static void getProductsByDescripcionCad(String cadena) {
        try (BaseXClient session = new BaseXClient("localhost", 1984, "admin", "abc123")) {
            String consulta = String.format("for $producto in db:get('productos')//producto\n" +
                    "where contains($producto/descripcion,'%s')\n" +
                    "order by  $producto/fabricante descending\n" +
                    "return concat(\n" +
                    "  'Nombre\t', $producto/nombre,'\tFabricante\t',$producto/fabricante\n" +
                    ")", cadena);
            BaseXClient.Query query = session.query(consulta);
            while (query.more()) {

                System.out.println(query.next());
            }
            System.out.println(query.info());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * for $producto in db:get('productos')//producto
     * group by $categoria:=$producto/categoria
     * let $cantidad:=count($producto/disponibilidad)
     * return concat(
     * 'Categoria	', $categoria,'	Cantidad	',$cantidad
     * )
     */
    public static void getCountbyCategoria() {
        String consulta = "let $totalCantidad := sum(db:get('productos')//producto/disponibilidad)\n" +
                "for $categoria in distinct-values(db:get('productos')//producto/categoria)\n" +
                "let $countProductos := db:get('productos')//producto[categoria = $categoria]\n" +
                "let $categoriaTotal := sum($countProductos/disponibilidad)\n" +
                "let $porcentaje := ($categoriaTotal div $totalCantidad) * 100\n" +
                "return concat(\n" +
                "  'Categoría\t', $categoria,' Cantidad\t', $categoriaTotal,\n" +
                "  ' - Porcentaje\t', xs:integer($porcentaje), '%'\n" +
                ")";
        try (BaseXClient session = new BaseXClient("localhost", 1984, "admin", "abc123")) {
            BaseXClient.Query query = session.query(consulta);
            while (query.more()) {
                System.out.println(query.next());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


// metodos de peticion de datos

    public static String pedirString(String mensaje) {
        while (true) {
            System.out.println(mensaje);
            String entrada = "";
            try {
                entrada = sc.next();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Debes introducir un string");

            }
            sc.nextLine();

            return entrada;
        }

    }

    public static int pedirInt(String mensaje) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println(mensaje);

            try {
                return sc.nextInt();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Debes introducir un string");

            }

        }

    }
}


//    public static String readXMLToString(String filePath) throws Exception {
//        File xmlFile = new File(filePath);
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document document = builder.parse(xmlFile);
//        // Normalize the XML structure
//        document.getDocumentElement().normalize();
//
//        // Convert Document to String
//        return documentToString(document);
//    }
//
//    public static String documentToString(Document document) throws Exception {
//       TransformerFactory tf = TransformerFactory.newInstance();
//        Transformer transformer = tf.newTransformer();
//        transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
//       StringWriter writer = new StringWriter();
//        transformer.transform(new DOMSource(document), new StreamResult(writer));
//        return writer.toString();
//    }

//    public static void crearBD(){
//        try(      BaseXClient session = new BaseXClient("localhost",1984,"admin","abc123")) {
//        final InputStream bais = new ByteArrayInputStream(readXMLToString("src/resources/productos.xml").getBytes());
//        session.create("productos",bais);
//        session.info();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//    }