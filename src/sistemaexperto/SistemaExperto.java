/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistemaexperto;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 *
 * @author Alberto Vázquez.
 */
public class SistemaExperto {

    public ArrayList<String> regla = new ArrayList<>();//Forma: C OP C OP ...
    public ArrayList<String> produccion = new ArrayList<>();//Forma: P P P ...
    public ArrayList<String> hechosSeleccion = new ArrayList<>();
    public ArrayList<String> hechosInicio = new ArrayList<>();
    public ArrayList<String> hechosSeleccionables = new ArrayList<>();
    String[] hechosInicioArray;// = {"F", "G", "H"};

    int cont = 0;
    InfixPostfix4 intf2 = new InfixPostfix4();
    //JTextField jTextFieldResultado;
    JTextArea jTextAreaResultado;
    JComboBox jComboBoxHechos;
    GUI gui;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //new SistemaExperto();
    }

    private void establecerHechosInicio() {
        for (int i = 0; i < hechosInicioArray.length; i++) {
            hechosInicio.add(hechosInicioArray[i]);
        }
    }

    /**
     * Va por cada regla e intenta encontrar los elementos de los hechos de
     * inicio Cada elemento a encontrar debe existir como produccion. De lo
     * contrario ahi se queda la induccion de la reglas
     */
    public void induccion(int index) {
        agregarTextoPanel("\nINICIANDO INDUCCION\n");
        this.imprimeHechosInicio();
        System.out.println("primeraInduccion: " + produccion.get(index));
        induccionProduccion(produccion.get(index));
        this.imprimeResultado();
    }

    public void abduccion(int index) {
        //while (true) {
        this.deduccion();
        this.induccion(index);
        //int opc = JOptionPane.showConfirmDialog(null, "Desea continuar?");
        //JOptionPane.showMessageDialog(null, "opc: "+opc);
        //if (opc == 1) {
        // break;
        //}
        //   }
    }

    boolean continuar;

    /**
     * Se induce una regla
     *
     * @param prod recibe como entrada la produccion de la misma
     */
    public void induccionProduccion(String prod) {
        System.out.println("llamando a induccionProduccion" + prod);
        //Recibe la produccion a inducir
        if (buscarRegla(prod) != null) {

            continuar = true;
            agregarTextoPanel("Induccion del consecuente: " + prod + " buscando: " + buscarRegla(prod) + "\n");
            String reglaTemp = buscarRegla(prod);
            String stringTemp = "", postfijo;
            System.out.print("revisando prod: " + prod + "\n");
            //La paso a notacion postfija y verifica si estan completos los compoenentes de la regla
            postfijo = intf2.ejecutar(reglaTemp, false);
            imprimeHechosInicio();
            System.out.println("postfijo: " + postfijo);
            if (deduccionRegla(postfijo)) {
                System.out.println("Produccion encontrada");
            } else {
                StringTokenizer tokens = new StringTokenizer(reglaTemp, "+ -");
                String[] tokensRevez = new String[tokens.countTokens()];
                for (int i = 0; tokens.hasMoreTokens(); i++) {
                    tokensRevez[i] = tokens.nextToken();
                }
                ArrayUtils.reverse(tokensRevez);
                for (int i = 0; i < tokensRevez.length; i++) {
                    String temp = tokensRevez[i];
                    //Debe de existir como producto
                    if (continuar) {
                        induccionProduccion(temp);
                    }
                }
                if (continuar) {
                    induccionProduccion(prod);
                }
            }
        } else if (this.buscarHechoInicio(prod)) {
            this.agregarHechoInicio(prod);

        } else {
            agregarTextoPanel("Induccion de la regla: " + prod + " no se encontro. ¿Desea agregarla?  \n");
            //Pregunta al usuario si el tiene el consecuente
            int opc = JOptionPane.showOptionDialog(null, //Component parentComponent
                    "La regla \"" + prod + "\" no se encontro como consecuente o hecho. ¿Desea agregarla?", //Object message,
                    "No se encontro un hecho", //String title
                    JOptionPane.YES_NO_OPTION, //int optionType
                    JOptionPane.INFORMATION_MESSAGE, //int messageType
                    null, //Icon icon,
                    new String[]{"Si", "No"}, //Object[] options,
                    "Metric");
            if (opc == 0) {
                //JOptionPane.showMessageDialog(null, "Agregando");
                this.agregarHechoInicio(prod);
                this.imprimeHechosInicio();
            } else {
                JOptionPane.showMessageDialog(null, "La inducción no puede continuar");
                agregarTextoPanel("La induccion se detuvo, no se encontro como hecho o consecuente");
                continuar = false;
            }
        }
        this.imprimeHechosInicio();
    }

    /**
     *
     * @param jTextAreaResultado
     * @param jComboBoxHechos
     * @param gui
     */
    public SistemaExperto(JTextArea jTextAreaResultado, JComboBox jComboBoxHechos, GUI gui) {
        this.jTextAreaResultado = jTextAreaResultado;
        //recogerDatos();
        analizarHechos();
        //establecerHechosInicio();
        System.out.println("IMPRIMIENDO REGLAS");
        //imprimeReglas();
        //pruebaDeduccion();
        //pruebaInduccion();
        this.gui = gui;
    }

    /**
     * Prueba
     */
    private void pruebaInduccion() {
        induccion(0);
    }

    /**
     * Prueba
     */
    private void pruebaDeduccion() {
        deduccion();

    }

    /**
     * Muestra los hechos de inicio actuales
     */
    public void imprimeHechosInicio() {
        //System.out.println("\nHECHOS INICIO");
        for (int i = 0; i < hechosInicio.size(); i++) {
            //System.out.println(hechosInicio.get(i));
        }
        //System.out.println("\n\n");
    }

    public void imprimeResultado() {
        agregarTextoPanel("\nRESULTADO\n");
        for (int i = 0; i < hechosInicio.size(); i++) {
            agregarTextoPanel(hechosInicio.get(i) + "\n");
        }
    }

    /**
     * Se usa este metodo para no permitir duplicar hechos de inicio
     *
     * @param hecho
     */
    public void agregarHechoInicio(String hecho) {
        //System.out.println("agregarHechoInicio: " + hecho);
        if (!buscarHechoInicio(hecho)) {
            hechosInicio.add(hecho);
        }
    }

    /**
     * Da una pasada con los hechos de inicio y si estan completos produce y la
     * produccion se agrega a hechos de inicio.
     */
    public void deduccion() {
        this.agregarTextoPanel("\nINICIANDO DEDUCCION\n");
        String[] tempRegla;
        String stringTemp, postfijo;
        for (int i = 0; i < regla.size(); i++) {
            cont = i;
            stringTemp = "";
            System.out.print("revisando regla: " + i);
            String vecReglaTemp = regla.get(i);

            System.out.println("\nvecReglaTemp[0]" + vecReglaTemp);
            postfijo = intf2.ejecutar(vecReglaTemp, false);
            //imprimeHechosInicio();
            //System.out.println(" postfijo: " + postfijo);
            deduccionRegla(postfijo);

            //System.out.println("");
        }
        //System.out.println("TERMINADA DEDUCCION ");
        //this.imprimeHechosInicio();
        this.imprimeResultado();
    }

    /**
     * Recibe la regla en notacion postfija y regresa lo que produjo (nada o n
     * reglas)
     *
     * @param reglaPost regla en notacion postfija
     * @return
     */
    public boolean deduccionRegla(String reglaPost) {
        //System.out.println("reglaPost: " + reglaPost);
        boolean deduccionTrue = false;
        Stack<String> pila1 = new Stack<>();//Para meter todos los caracteres en orden
        Stack<String> pila2 = new Stack<>();//Para meter los resultados
        String ultimoPeek;
        String[] arrayPost = reglaPost.split(" ");
        ArrayUtils.reverse(arrayPost);
        for (String tokensVector1 : arrayPost) {
            //System.out.println(tokensVector1);
            pila1.push(tokensVector1);
        }
        while (pila1.size() != 0) {
            String temp1, temp2;
            ultimoPeek = pila1.peek();
            System.out.println("ultimoPeek: " + ultimoPeek);
            if (ultimoPeek.equals("+") || ultimoPeek.equals("-")) {
                System.out.println("OPERADOR");
                switch (ultimoPeek) {
                    case "+":
                        pila1.pop();
                        //System.out.println("op1: " + pila2.peek());
                        temp1 = pila2.pop(); //El primer op
                        //System.out.println("op2: " + pila2.peek());
                        temp2 = pila2.pop(); //EL segundo op
                        if ((temp1.equals("true") || buscarHechoInicio(temp1)) && (temp2.equals("true") || buscarHechoInicio(temp2))) {
                            //Agrego "true" a la pila
                            pila2.push("true");
                            //System.out.println("agregando true");
                        } else {
                            pila2.push("false");
                            //System.out.println("no produjo: " + temp1 + "y " + temp2);
                            break;
                        }
                        break;
                    case "-":
                        pila1.pop();
                        //System.out.println("op1: " + pila2.peek());
                        temp1 = pila2.pop(); //El primer op
                        //System.out.println("op2: " + pila2.peek());
                        temp2 = pila2.pop(); //EL segundo op
                        if ((temp1.equals("true") || buscarHechoInicio(temp1)) || (temp2.equals("true") || buscarHechoInicio(temp2))) {
                            pila2.push("true");
                            //System.out.println("agregando true");
                        } else {
                            pila2.push("false");
                            //System.out.println("no produjo: " + temp1 + "y " + temp2);
                            break;
                        }
                        break;
                }
            } else {
                System.out.println("LETRA");
                pila2.push(ultimoPeek);
                pila1.pop();
                if (pila1.empty()) {
                    deduccionTrue = true;
                    if (this.buscarHechoInicio(ultimoPeek)) {
                        //System.out.println("VACIO EN LETRA. AGREGANDO PRODUCCION");
                        //Aqui se debe de agregar la produccion de
                        String produjo = buscarProduccion(ultimoPeek);
                        //String produjo = produccion.get(cont);
                        agregarTextoPanel("Se encontraron hechos de la regla: " + buscarReglaPostfija(reglaPost) + " entonces " + produjo + "\n");
                        agregarHechoInicio(produjo);
                        agregarTextoPanel("agregando a hechos: " + produjo + "\n");
                    } else {
                        deduccionTrue = false;
                        System.out.println("VACIO EN LETRA. NO AGREGA PRODUCCION");
                    }
                }
            }
        }
        if (pila2.peek().equals("true")) {
            deduccionTrue = true;
            //produjo
            //System.out.println("ULTIMO PILA TRUE. AGREGANDO PRODUCCION");
            String produjo = buscarProduccionPostfija(reglaPost);//reglaPost
            agregarTextoPanel("Se encontraron hechos de la regla: " + buscarReglaPostfija(reglaPost) + " entonces " + produjo + "\n");
            agregarHechoInicio(produjo);
            agregarTextoPanel("agregando a hechos: " + produjo + "\n");
        }

        //System.out.println("ultimoPila2: " + pila2.peek());
        System.out.println("return: " + deduccionTrue);
        return deduccionTrue;
    }

    public void agregarTextoPanel(String txt) {
        System.out.print(txt);
        String temp = this.jTextAreaResultado.getText();
        temp += txt;
        jTextAreaResultado.setText(temp);
    }

    public void borrarTextoPanel(String txt) {

    }

    /**
     * De acuerdo al hecho da la produccion
     *
     * @param hecho
     * @return
     */
    public String buscarProduccion(String hecho) {
        for (int i = 0; i < regla.size(); i++) {
            if (regla.get(i).equals(hecho)) {
                return produccion.get(i);
            }
        }
        return null;
    }

    /**
     * Recibe la regla en notacion postfija y regresa el producto
     *
     * @param post
     * @return
     */
    public String buscarProduccionPostfija(String post) {
        for (int i = 0; i < regla.size(); i++) {
            if (this.intf2.ejecutar(regla.get(i), false).equals(post)) {
                return produccion.get(i);
            }
        }
        return null;
    }

    /**
     * Recibe la regla en notacion postfija y regres la regla
     *
     * @param post
     * @return
     */
    public String buscarReglaPostfija(String post) {
        for (int i = 0; i < regla.size(); i++) {
            if (this.intf2.ejecutar(regla.get(i), false).equals(post)) {
                return regla.get(i);
            }
        }
        return null;
    }

    /**
     * Recibida la produccion regresa el string de reglas requeridas. (busca una
     * una regla del lado derecho de las producicones)
     *
     * @param prod
     * @return
     */
    public String buscarRegla(String prod) {
        //System.out.println("buscando regla " + prod);
        for (int i = 0; i < produccion.size(); i++) {
            if (produccion.get(i).equals(prod)) {
                //System.out.println("busqueda completada en " + i + ": " + regla.get(i));
                return regla.get(i);
            }
        }
        return null;
    }

    /**
     * Busca el hecho y regresa true si lo encuentra
     *
     * @param hecho
     * @return
     */
    public boolean buscarHechoInicio(String hecho) {
        System.out.println("buscando hecho: " + hecho);
        for (int i = 0; i < hechosInicio.size(); i++) {
            String hecchosInicio1 = hechosInicio.get(i);
            //System.out.println("comparando: " + hecchosInicio1 + "<->" + hecho);
            if (hecchosInicio1.equals(hecho)) {
                System.out.println("hecho iniciEncontrado: " + hecho);
                return true;
            }
        }
        System.out.println("hecho no se encontro ");
        return false;
    }

    /**
     * Imprime las reglas de produccion con su respectiva produccion
     */
    public void imprimeReglas() {
        //System.out.println("REGLAS");
        String tempRegla, tempProduccion;
        for (int i = 0; i < regla.size(); i++) {
            tempRegla = regla.get(i);
            //System.out.println(tempRegla);
            tempProduccion = produccion.get(i);
            //System.out.print("->");
            //System.out.print(tempProduccion);
            //System.out.println();
        }

    }

    /**
     * busca dentro de las reglas los posibles hechos y los enlista en el
     * comboBox
     */
    public void analizarHechos() {
        hechosSeleccion = new ArrayList<>();
        for (int i = 0; i < this.produccion.size(); i++) {

        }
    }

    public String buscarHechoProduccion(String hecho) {
        //System.out.println("buscando hecho " + hecho);
        for (int i = 0; i < hechosSeleccion.size(); i++) {
            if (hechosSeleccion.get(i).equals(hecho)) {
                //System.out.println("busqueda completada en " + i + ": " + hechosSeleccion.get(i));
                return hechosSeleccion.get(i);
            }
        }
        return null;
    }

    /**
     * Recoge datos del excel
     */
    private void recogerDatos() {
        //System.out.println("ok");
        try {
            File inputWorkbook = new File("C:\\Users\\Alberto\\Desktop\\DatosSistemaExperto.xls");
            Workbook w;
            w = Workbook.getWorkbook(inputWorkbook);
            Sheet sheet = w.getSheet(0);
            //System.out.println("columnas: " + sheet.getColumns());
            //System.out.println("filas :" + sheet.getRows());
            //Primera columna (Reglas)
            //System.out.println("REGLAS");
            for (int i = 0; i < sheet.getRows(); i++) {
                Cell cell = sheet.getCell(0, i);
                CellType type = cell.getType();
                //System.out.println(cell.getContents());
                String temp = cell.getContents();
                StringTokenizer tokens = new StringTokenizer(temp, " ");
                String vecRegla = "";
                int k = 0;
                while (tokens.hasMoreTokens()) {
                    vecRegla += tokens.nextToken();
                    //System.out.println(vecRegla[k]);
                    k++;
                }
                regla.add(vecRegla);
            }
            //Segunda columna (Producciones)
            //System.out.println("PRODUCCIONES");
            for (int i = 0; i < sheet.getRows(); i++) {
                Cell cell = sheet.getCell(1, i);
                CellType type = cell.getType();
                //System.out.println(cell.getContents());
                String temp = cell.getContents();
                StringTokenizer tokens = new StringTokenizer(temp, " ");
                String vecProduccion = "";
                int k = 0;
                while (tokens.hasMoreTokens()) {
                    vecProduccion += tokens.nextToken();
                    //System.out.println(vecProduccion[k]);
                    k++;
                }
                produccion.add(vecProduccion);
            }
            //System.out.println("Num: " + numeroDeFF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void agregarReglaProduccion(String r) {
        String split[] = r.split("->");
        regla.add(split[0]);
        produccion.add(split[1]);//Produccion
        //imprimeReglas();
        gui.actualizarListaReglaProduccion();
    }

    public void agregarHechoSeleccionable(String hecho) {
        boolean existe = false;
        for (int i = 0; i < this.hechosSeleccionables.size(); i++) {
            if (hechosSeleccionables.get(i).equals(hecho)) {
                existe = true;
            }
        }
        if (!existe) {
            System.out.println("agregando hecho seleccionable: " + hecho);
            hechosSeleccionables.add(hecho);
        }
    }

    /**
     * Carga la base de conocimientos en formato .txt
     */
    public void cargarBaseConocimiento1() {
        regla = new ArrayList<String>();
        produccion = new ArrayList<String>();
        hechosSeleccionables = new ArrayList<String>();

        boolean encontradoProducto = true; //SI encuentra un : lo activa, SI encuenta un & lo desactiva
        ArrayList<String> regla = new ArrayList<>();
        //System.out.println("cargarBaseConocimiento1");
        try {
            // Abrimos el archivo
            FileInputStream fstream = new FileInputStream("C:\\Users\\Alberto\\Documents\\NetBeansProjects\\SE\\reglas_enfermedades.txt");
            // Creamos el objeto de entrada
            DataInputStream entrada = new DataInputStream(fstream);
            // Creamos el Buffer de Lectura
            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
            String strLinea;
            // Leer el archivo linea por linea
            while ((strLinea = buffer.readLine()) != null) {
                if (strLinea == null) {
                    //System.out.println("2null");
                } else {
                    //System.out.println("2nonull");
                }
                //strLinea = buffer.readLine();
                String reglaTemp = "";
                // Imprimimos la línea por pantalla
                //System.out.println(strLinea);
                char[] tokens = strLinea.toCharArray();
                int contComilla = 0;
                boolean comillaAbierta = false;
                encontradoProducto = false;
                String condicion = "";
                for (int i = 0; i < tokens.length; i++) {
                    if (tokens[i] == '\'') {
                        contComilla++;
                        if (contComilla % 2 == 1) {
                            i++;
                            comillaAbierta = true;
                            if(encontradoProducto){
                               // encontradoProducto = false;
                            }
                            
                            System.out.println("abierta");
                        } else {
                            System.out.println("agregando condicion: " + condicion);
                            reglaTemp += condicion;
                            if (!encontradoProducto) {
                                System.out.println("agregando condicion2: " + condicion);
                                this.agregarHechoSeleccionable(condicion);
                            } else {
                                //encontradoProducto = false;
                                encontradoProducto = false;
                                System.out.println("Seteando: encontradoProducto: " + encontradoProducto);
                            }


                            condicion = "";
                            //System.out.println("1reglaTemp: " + reglaTemp);
                            comillaAbierta = false;
                            encontradoProducto = false;
                            System.out.println("cerrada");
                        }
                    } else if (tokens[i] == '&') {
                        reglaTemp += '+';
                        encontradoProducto = false;
                        System.out.println("encontradoProducto: " + encontradoProducto);
                        //System.out.println("2reglaTemp: " + reglaTemp);
                    } else if (tokens[i] == ':') {
                        encontradoProducto = true;
                        System.out.println("encontradoProducto: " + encontradoProducto);
                        System.out.println("producto");
                        reglaTemp += "->";
                    } else if (tokens[i] == ' ') {
                        //reglaTemp += '_';
                    } else if (tokens[i] == 10 || tokens[i] == 13) {
                        System.out.println("salto");
                        encontradoProducto = false;
                    }else if(Character.isDigit(tokens[i] )){
                         encontradoProducto = false;
                         System.out.println("digito");
                    }
                    else if(tokens[i] == '$'){
                        encontradoProducto = false;
                        System.out.println("peso");
                    }
                    if (comillaAbierta) {
                        if (tokens[i] == ' ') {
                            condicion += '_';
                        } else if (tokens[i] == '(' || tokens[i] == ')') {
                            condicion += '_';
                        } else {
                            condicion += tokens[i];
                        }

                    } else {
                    }
                }
                //System.out.println("reglaTemp: " + reglaTemp);
                agregarReglaProduccion(reglaTemp);
            }
            // Cerramos el archivo
            entrada.close();
        } catch (Exception e) { //Catch de excepciones
            e.printStackTrace();
        }
        gui.actualizarListaHechosSeleccionables();
    }

}
