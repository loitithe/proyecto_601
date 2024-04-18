import org.basex.examples.api.BaseXClient;

import java.io.*;
import java.util.Scanner;

public class Main {
    private static Scanner sc;
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
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;


        }


    }

    public static void main(String[] args) {
        sc = new Scanner(System.in);
    //    menuXML();
        
    }

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