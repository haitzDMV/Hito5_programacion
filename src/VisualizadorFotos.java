import org.jdesktop.swingx.JXDatePicker;
import org.mariadb.jdbc.export.Prepare;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class VisualizadorFotos extends JFrame {
    public static void visualizadorFotos() {

        JFrame jFrame = new JFrame("Photography");
        jFrame.setLayout(new GridLayout(3,2));


        //JPanel donde se muestran los nombres de los fotografos
        JPanel fotografos = new JPanel();
        JLabel jlfotografos = new JLabel("Photographer:");
        JComboBox jcfotografos = new JComboBox();

        //Mostrar todos los fotografos
        ArrayList<Fotografo> f = listaFotografos();
        for (Fotografo e: f) {
            jcfotografos.addItem(e.getNombre());
        }


        fotografos.add(jlfotografos);
        fotografos.add(jcfotografos);
        jFrame.add(fotografos);


        //Jpanel donde estará el calendario
        JPanel fecha = new JPanel();
        JLabel jlfotos = new JLabel("Photos after:");
        JXDatePicker date = new JXDatePicker();
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");


        //ActionListener para obtener la fecha
        final java.sql.Date[] fechaSQL = new java.sql.Date[1];
        ActionListener l = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (JXDatePicker.COMMIT_KEY.equals(e.getActionCommand())) {
                    date.setDate(date.getDate());
                    Date fechaSelec = date.getDate();
                    fechaSQL[0] = new java.sql.Date(fechaSelec.getTime());
                }
            }
        };

        date.addActionListener(l);


        fecha.add(jlfotos);
        fecha.add(date);
        jFrame.add(fecha);


        //JPanel para la lista de las fotografias
        JPanel lista = new JPanel();
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> jList = new JList<>(model);
        jList.setPreferredSize(new Dimension(200,150));
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(jList);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);


        //Action Listener para buscar las fotografias asignadas a cada fotografo
        jcfotografos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.clear();
                if (fechaSQL[0]==null) {
                    ArrayList<String> a = buscarFotografias(jcfotografos.getSelectedIndex());
                    for (String fotos: a) {
                        model.addElement(fotos);
                    }
                }else {
                    ArrayList<String> a = buscarFotografias(jcfotografos.getSelectedIndex(), fechaSQL[0]);
                    for (String fotos: a) {
                        model.addElement(fotos);
                    }
                }
            }
        });

        //Jpanel para la imagen
        JPanel panelImagen = new JPanel();
        JLabel imagen = new JLabel();
        imagen.setPreferredSize(new Dimension(200,200));

        //Action Listener para mostrar la imagen seleccionada
        jList.addListSelectionListener(e -> {
            String imagenSeleccionada = jList.getSelectedValue();
            Fotografias f2 = devuelveFotografia(imagenSeleccionada, jcfotografos.getSelectedIndex());
            if (f2 != null) {
                ImageIcon icon = new ImageIcon(f2.getArchivo());
                imagen.setIcon(icon);
                incrementarVisitas(f2);
            }
        });

        lista.add(scrollPane);
        jFrame.add(lista);

        panelImagen.add(imagen);
        jFrame.add(panelImagen);


        JPanel botones = new JPanel();
        JButton award = new JButton("AWARD");
        award.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int min = Integer.parseInt(JOptionPane.showInputDialog(null, "Minimo de visitas para recibir un premio:"));
                System.out.println(min);
                premiarFotografos(min, createVisitsMap());
            }
        });


        JButton remove = new JButton("REMOVE");
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarFotos();
            }
        });
        botones.add(award);
        botones.add(remove);
        jFrame.add(botones);


        jFrame.setPreferredSize(new Dimension(500,400));
        jFrame.pack();
        jFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        jFrame.setVisible(true);
    }


    public static void main(String[] args) {
        visualizadorFotos();
    }

    //METODOS

    //Obtener lista de todos los fotogrados de la DB
    public static ArrayList<Fotografo> listaFotografos() {
        conexion conexion = new conexion();
        Connection conn=conexion.MyConexion();
        ArrayList<Fotografo> ALfotografos = new ArrayList<>();

        try (PreparedStatement select = conn.prepareStatement("Select * from fotografo")) {
            ResultSet res = select.executeQuery();
            while (res.next()) {
                int id = res.getInt("iDfotografo");
                String nombre = res.getString("nombre");
                Boolean premiado = res.getBoolean("premiado");

                Fotografo f = new Fotografo(id,nombre,premiado);

                ALfotografos.add(f);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ALfotografos;
    }

    //Obtener fotografias de un fotografo
    public static ArrayList<String> buscarFotografias(int idFotografo,Date fecha) {
        ArrayList<String> ALreturn =new ArrayList<>();
        conexion conexion = new conexion();
        Connection conn=conexion.MyConexion();
        try(PreparedStatement select = conn.prepareStatement("SELECT * from fotos where IDfotografo = ? AND fecha <= ?")) {
            select.setInt(1,idFotografo+1);
            select.setDate(2, (java.sql.Date) fecha);
            ResultSet res = select.executeQuery();
            while (res.next()) {
                String titulo = res.getString("titulo");
                ALreturn.add(titulo);
            }

        }catch (SQLException e) {
            e.printStackTrace();
        }
        return ALreturn;
    }

    public static ArrayList<String> buscarFotografias(int idFotografo) {
        ArrayList<String> ALreturn =new ArrayList<>();
        conexion conexion = new conexion();
        Connection conn=conexion.MyConexion();
        try(PreparedStatement select = conn.prepareStatement("SELECT * from fotos where IDfotografo = ?")) {
            select.setInt(1,idFotografo+1);
            ResultSet res = select.executeQuery();
            while (res.next()) {
                String titulo = res.getString("titulo");
                ALreturn.add(titulo);
            }

        }catch (SQLException e) {
            e.printStackTrace();
        }
        return ALreturn;
    }


    //Devuelve objeto Fotografia
    public static Fotografias devuelveFotografia(String nombreFoto,int idFotografo) {
        Fotografias f = null;
        conexion conexion = new conexion();
        Connection conn = conexion.MyConexion();
        try (PreparedStatement select = conn.prepareStatement("SELECT * from fotos where titulo = ? AND IDfotografo = ?")){
            select.setNString(1,nombreFoto);
            select.setInt(2,idFotografo+1);
            ResultSet res = select.executeQuery();

            //Sin el if da error
            if (res.next()) {
                int ID = res.getInt("IDfoto");
                String titulo = res.getString("titulo");
                Date fecha = res.getDate("fecha");
                String fichero = res.getString("fichero");
                int visitas = res.getInt("visitas");
                int IDfotografo = res.getInt("IDfotografo");

                f = new Fotografias(ID,titulo,fecha,fichero,visitas,IDfotografo);

            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("ERROR: MOSTRAR FOTOGRAFIA");
        }
        return f;
    }

    //Hace +1 a las visitas
    public static void incrementarVisitas(Fotografias f) {
        int ID = f.getIdFoto();
        conexion conexion = new conexion();
        Connection conn = conexion.MyConexion();

        try(PreparedStatement select = conn.prepareStatement("SELECT visitas from fotos where IDfoto = ?")) {
            select.setInt(1,ID);
            ResultSet res1 = select.executeQuery();
            if (res1.next()) {
                int visitas = res1.getInt("visitas") + 1;

                try(PreparedStatement update = conn.prepareStatement("UPDATE fotos SET visitas = ? where IDfoto = ?")) {
                    update.setInt(1,visitas);
                    update.setInt(2,ID);
                    update.executeUpdate();
                } catch (SQLException r) {
                    r.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<Integer,Integer> createVisitsMap() {
        HashMap<Integer,Integer> visitas = new HashMap<>();
        Connection conn = conexion.MyConexion();

        try(PreparedStatement select = conn.prepareStatement("SELECT fotografo.IDfotografo, sum(visitas) from fotografo, fotos where fotografo.IDfotografo=fotos.IDfotografo GROUP BY fotografo.IDfotografo")) {
            ResultSet res= select.executeQuery();
            while (res.next()) {
                int id = res.getInt(1);
                int vis = res.getInt(2);
                visitas.put(id,vis);
            }

            return visitas;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void premiarFotografos(int min, HashMap<Integer,Integer> mapaVisitas) {
        Connection conn = conexion.MyConexion();

        Iterator<Integer> it = mapaVisitas.keySet().iterator();
        while (it.hasNext()) {
            int clave = (int) it.next();
            int valor = mapaVisitas.get(clave);
            if (valor>=min) {
                try(PreparedStatement update = conn.prepareStatement("UPDATE fotografo SET premiado = 1 where IDfotografo = ?")) {
                    update.setInt(1,clave);
                    update.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void eliminarFotos() {
        Connection conn = conexion.MyConexion();


        try(PreparedStatement select = conn.prepareStatement("SELECT * FROM fotos f, fotografo fg where f.visitas=0 AND fg.premiado=0 AND f.IDfotografo=fg.IDfotografo")) {
            ResultSet res = select.executeQuery();

            //Preguntar por cada resultado si quiere eliminar la foto
            while(res.next()) {
                String nombre = res.getString("f.titulo");
                String autor = res.getString("fg.nombre");
                int idFoto=res.getInt("f.IDfoto");

                int respuesta=JOptionPane.showConfirmDialog(null,"¿Desea eliminar la foto " + nombre + " del autor " + autor + "?");

                if (JOptionPane.OK_OPTION == respuesta){
                    System.out.println("Selecciona opción Afirmativa");
                    try(PreparedStatement delete = conn.prepareStatement("DELETE FROM fotos where IDfoto=?")) {
                        delete.setInt(1,idFoto);
                        delete.executeUpdate();
                    }catch (SQLException e) {
                        System.out.println("ERROR: 2");
                        e.printStackTrace();
                    }
                }
            }
        }catch (SQLException e) {
            System.out.println("ERROR: 1");
            e.printStackTrace();
        }


        //Eliminar fotografos sin fotos
        try(PreparedStatement select2 = conn.prepareStatement("SELECT IDfotografo FROM fotografo WHERE IDfotografo NOT IN (SELECT DISTINCT IDfotografo FROM fotos)")) {
            ResultSet res2=select2.executeQuery();
            while (res2.next()) {
                int id=res2.getInt("IDfotografo");
                try(PreparedStatement delete2 = conn.prepareStatement("DELETE FROM fotografo WHERE IDfotografo=?")) {
                    delete2.setInt(1,id);
                    delete2.executeUpdate();
                }catch (SQLException e) {
                    System.out.println("ERROR: 4");
                    e.printStackTrace();
                }
            }

        }catch(SQLException e) {
            System.out.println("ERROR: 3");
            e.printStackTrace();
        }

    }
}