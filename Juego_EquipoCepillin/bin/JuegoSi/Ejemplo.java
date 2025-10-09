package mis_juegos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.awt.geom.AffineTransform;

public class MarioBros extends JPanel implements ActionListener {
    private Image personaje;
    private Image quieto;
    private Image saltando;
    private Image izquierda;
    private Image derecha;
    private Image fondo;
    private Image abajo;
    private Image imagenBala;
    private Image imagenPlaneta;
    private Image imagenExplosion;
    private Image laser; // Imagen del láser

    // Tamaño del personaje
    private final int ANCHO_PERSONAJE = 100;
    private final int ALTO_PERSONAJE = 80;

    // Tamaño de la ventana
    private final int ANCHO_VENTANA = 1000;
    private final int ALTO_VENTANA = 700;

    // Tamaño de las balas
    private final int ANCHO_BALA = 30;
    private final int ALTO_BALA = 30;
    
    // Tamaño de los láseres (fijos, ya que se rotan)
    private final int ANCHO_LASER = 80;
    private final int ALTO_LASER = 20;

    // Tamaño de los planetas
    private final int ANCHO_PLANETA = 80;
    private final int ALTO_PLANETA = 80;

    private int x = 400, y = 550; // Posición inicial (abajo centro)
    private int velocidad = 10;

    // Listas para almacenar las balas, planetas y láseres
    private ArrayList<Bala> balas;
    private ArrayList<Planeta> planetas;
    private ArrayList<Explosion> explosiones;
    private ArrayList<Laser> lasers; // Lista de láseres
    private Timer timer;
    private int direccionActual = 2; // 1: derecha, -1: izquierda, 2: arriba, -2: abajo (Inicia apuntando arriba)
    private Random random;
    private int planetasDestruidos = 0;
    private boolean jugadorVivo = true; // Estado del jugador

    // Constantes de tiempo
    private final int TIEMPO_ENTRE_DISPAROS_PLANETA = 67; 
    private int contadorDisparoPlaneta = 0;
    
    // VARIABLES GLOBALES para el control de carga y distancia
    private Planeta planetaCargando = null; 
    private final int DISTANCIA_MINIMA_DISPARO = 200; 
    
    // NUEVAS BANDERAS QOL PARA MOVIMIENTO CONTINUO
    private boolean movingRight = false;
    private boolean movingLeft = false;
    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean isShooting = false; 

    // Clase interna para representar una bala (del jugador)
    private class Bala {
        int x, y;
        int direccion; 
        int velocidadBala = 15;

        public Bala(int x, int y, int direccion) {
            this.x = x;
            this.y = y;
            this.direccion = direccion;
        }

        public void mover() {
            if (direccion == 1) { x += velocidadBala;
            } else if (direccion == -1) { x -= velocidadBala;
            } else if (direccion == 2) { y -= velocidadBala;
            } else if (direccion == -2) { y += velocidadBala;
            }
        }

        public boolean fueraDePantalla() {
            return x < -ANCHO_BALA || x > ANCHO_VENTANA ||
                   y < -ALTO_BALA || y > ALTO_VENTANA;
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, ANCHO_BALA, ALTO_BALA);
        }
    }

    // Clase interna para representar un láser (del planeta)
    private class Laser {
        double x, y; 
        double velX, velY; 
        int velocidadLaser = 12;

        public Laser(int startX, int startY, double targetX, double targetY) {
            this.x = startX;
            this.y = startY;
            
            double dx = targetX - startX;
            double dy = targetY - startY;
            double distancia = Math.sqrt(dx * dx + dy * dy);
            
            if (distancia > 0) {
                velX = (dx / distancia) * velocidadLaser;
                velY = (dy / distancia) * velocidadLaser;
            } else {
                velX = 0;
                velY = -velocidadLaser;
            }
        }

        public void mover() {
            x += velX;
            y += velY;
        }

        public boolean fueraDePantalla() {
            return x < -ANCHO_LASER || x > ANCHO_VENTANA ||
                   y < -ALTO_LASER || y > ALTO_VENTANA;
        }

        public Rectangle getBounds() {
            return new Rectangle((int)x, (int)y, ANCHO_LASER, ALTO_LASER);
        }
    }

    // Clase interna para representar un planeta
    private class Planeta {
        int x, y;

        public Planeta(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, ANCHO_PLANETA, ALTO_PLANETA);
        }

        public void dispararLaser() {
            int laserStartX = x + ANCHO_PLANETA / 2 - ANCHO_LASER / 2;
            int laserStartY = y + ALTO_PLANETA / 2 - ALTO_LASER / 2;

            double targetX = MarioBros.this.x + ANCHO_PERSONAJE / 2;
            double targetY = MarioBros.this.y + ALTO_PERSONAJE / 2;
            
            lasers.add(new Laser(laserStartX, laserStartY, targetX, targetY));
        }
    }

    // Clase interna para representar una explosión
    private class Explosion {
        int x, y;
        int duracion;

        public Explosion(int x, int y) {
            this.x = x;
            this.y = y;
            this.duracion = 15;
        }
    }

    public MarioBros() {
        setPreferredSize(new Dimension(ANCHO_VENTANA, ALTO_VENTANA));
        balas = new ArrayList<>();
        planetas = new ArrayList<>();
        explosiones = new ArrayList<>();
        lasers = new ArrayList<>();
        random = new Random();

        // Cargar imágenes
        try {
            ImageIcon iconSaltando = new ImageIcon("imagenes_mario/UP.PNG");
            ImageIcon iconQuieto = new ImageIcon("imagenes_mario/FRONT.PNG");
            ImageIcon iconIzquierda = new ImageIcon("imagenes_mario/LEFT.PNG");
            ImageIcon iconDerecha = new ImageIcon("imagenes_mario/RIGHT.PNG");
            ImageIcon iconAbajo = new ImageIcon("imagenes_mario/DOWN.PNG");
            ImageIcon iconFondo = new ImageIcon("imagenes_mario/FONDO.jpg");
            ImageIcon iconBala = new ImageIcon("imagenes_mario/BALA.jpg");
            ImageIcon iconPlaneta = new ImageIcon("imagenes_mario/PLANETA.PNG");
            ImageIcon iconExplosion = new ImageIcon("imagenes_mario/BUM.jpg");
            ImageIcon iconLaser = new ImageIcon("imagenes_mario/LASER.PNG"); 

            saltando = iconSaltando.getImage();
            quieto = iconQuieto.getImage();
            izquierda = iconIzquierda.getImage();
            derecha = iconDerecha.getImage();
            abajo = iconAbajo.getImage();
            fondo = iconFondo.getImage();
            imagenBala = iconBala.getImage();
            imagenPlaneta = iconPlaneta.getImage();
            imagenExplosion = iconExplosion.getImage();
            laser = iconLaser.getImage(); 

            personaje = quieto;

            System.out.println("Imágenes cargadas correctamente");
        } catch (Exception e) {
            System.out.println("Error al cargar imágenes: " + e.getMessage());
        }

        // Crear planetas iniciales (3 planetas)
        for (int i = 0; i < 3; i++) {
            crearPlanetaAleatorio();
        }

        setFocusable(true);

        // Timer para actualizar la lógica del juego
        timer = new Timer(30, this); 
        timer.start();

        // INICIO: KEYLISTENER PARA MOVIMIENTO FLUIDO
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!jugadorVivo) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        reiniciarJuego();
                    }
                    return;
                }

                int tecla = e.getKeyCode();

                // Activar banderas de movimiento
                if (tecla == KeyEvent.VK_D) {
                    movingRight = true;
                    direccionActual = 1;
                } else if (tecla == KeyEvent.VK_A) {
                    movingLeft = true;
                    direccionActual = -1;
                } else if (tecla == KeyEvent.VK_W) {
                    movingUp = true;
                    direccionActual = 2;
                } else if (tecla == KeyEvent.VK_S) {
                    movingDown = true;
                    direccionActual = -2;
                } else if (tecla == KeyEvent.VK_SPACE) {
                    if (!isShooting) {
                        disparar();
                        isShooting = true; 
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!jugadorVivo) return;

                int tecla = e.getKeyCode();

                // Desactivar banderas de movimiento
                if (tecla == KeyEvent.VK_D) {
                    movingRight = false;
                } else if (tecla == KeyEvent.VK_A) {
                    movingLeft = false;
                } else if (tecla == KeyEvent.VK_W) {
                    movingUp = false;
                } else if (tecla == KeyEvent.VK_S) {
                    movingDown = false;
                } else if (tecla == KeyEvent.VK_SPACE) {
                    isShooting = false;
                }

                // Determinar la imagen estática correcta (quieto) si NO se está moviendo
                if (!movingRight && !movingLeft && !movingUp && !movingDown) {
                    personaje = quieto;
                }
            }
        });
        // FIN: KEYLISTENER PARA MOVIMIENTO FLUIDO
    }

    private void reiniciarJuego() {
        jugadorVivo = true;
        x = 400; 
        y = 550;
        direccionActual = 2; 
        planetasDestruidos = 0;
        balas.clear();
        planetas.clear();
        explosiones.clear();
        lasers.clear();
        contadorDisparoPlaneta = 0;
        planetaCargando = null; 
        
        // Resetear banderas de movimiento
        movingRight = movingLeft = movingUp = movingDown = isShooting = false;

        for (int i = 0; i < 3; i++) {
            crearPlanetaAleatorio();
        }

        personaje = quieto; 
        timer.start(); 
        repaint();
    }


    private void crearPlanetaAleatorio() {
        int planetaX = random.nextInt(ANCHO_VENTANA - ANCHO_PLANETA);
        int planetaY = random.nextInt(ALTO_VENTANA - ALTO_PLANETA);
        planetas.add(new Planeta(planetaX, planetaY));
    }

    private void disparar() {
        int balaX = x + ANCHO_PERSONAJE / 2 - ANCHO_BALA / 2;
        int balaY = y + ALTO_PERSONAJE / 2 - ALTO_BALA / 2;
        balas.add(new Bala(balaX, balaY, direccionActual));
    }
    
    private boolean estaCercaDelJugador(Planeta planeta) {
        int centroPlanetaX = planeta.x + ANCHO_PLANETA / 2;
        int centroPlanetaY = planeta.y + ALTO_PLANETA / 2;
        
        int centroJugadorX = x + ANCHO_PERSONAJE / 2;
        int centroJugadorY = y + ALTO_PERSONAJE / 2;

        double distanciaCuadrada = Math.pow(centroPlanetaX - centroJugadorX, 2) + 
                                   Math.pow(centroPlanetaY - centroJugadorY, 2);

        return distanciaCuadrada < Math.pow(DISTANCIA_MINIMA_DISPARO, 2);
    }

    private void verificarColisiones() {
        // 1. Balas (jugador) vs. Planetas
        Iterator<Bala> iteradorBalas = balas.iterator();
        while (iteradorBalas.hasNext()) {
            Bala bala = iteradorBalas.next();
            Iterator<Planeta> iteradorPlanetas = planetas.iterator();

            while (iteradorPlanetas.hasNext()) {
                Planeta planeta = iteradorPlanetas.next();

                if (bala.getBounds().intersects(planeta.getBounds())) {
                    explosiones.add(new Explosion(planeta.x, planeta.y));
                    iteradorBalas.remove();
                    iteradorPlanetas.remove();
                    planetasDestruidos++;
                    crearPlanetaAleatorio();
                    
                    if (planeta == planetaCargando) {
                        planetaCargando = null;
                        contadorDisparoPlaneta = 0; 
                    }
                    
                    break;
                }
            }
        }

        // 2. Láseres (planetas) vs. Jugador
        if (jugadorVivo) {
            Rectangle boundsPersonaje = new Rectangle(x, y, ANCHO_PERSONAJE, ALTO_PERSONAJE);
            Iterator<Laser> iteradorLasers = lasers.iterator();

            while (iteradorLasers.hasNext()) {
                Laser laser = iteradorLasers.next();
                if (laser.getBounds().intersects(boundsPersonaje)) {
                    explosiones.add(new Explosion(x, y)); 
                    jugadorVivo = false;
                    iteradorLasers.remove();
                    break;
                }
            }
        }
    }

    private void actualizarLogicaPlanetas() {
        if (!jugadorVivo) return;

        contadorDisparoPlaneta++;
        if (contadorDisparoPlaneta >= TIEMPO_ENTRE_DISPAROS_PLANETA) {
            
            Planeta planetaADisparar = planetaCargando; 
            
            if (planetaADisparar == null && !planetas.isEmpty()) {
                planetaADisparar = planetas.get(random.nextInt(planetas.size()));
            }

            if (planetaADisparar != null) {
                if (!estaCercaDelJugador(planetaADisparar)) {
                    planetaADisparar.dispararLaser();
                }
            }
            
            contadorDisparoPlaneta = 0;
            planetaCargando = null;
        }
    }
    
    // NUEVO MÉTODO: Ejecuta el movimiento continuo basado en las banderas
    private void moverPersonaje() {
        if (!jugadorVivo) return;

        // Movimiento horizontal
        if (movingRight) {
            personaje = derecha;
            direccionActual = 1;
            x = Math.min(x + velocidad, ANCHO_VENTANA - ANCHO_PERSONAJE);
        } else if (movingLeft) {
            personaje = izquierda;
            direccionActual = -1;
            x = Math.max(x - velocidad, 0);
        }

        // Movimiento vertical
        if (movingUp) {
            personaje = saltando;
            direccionActual = 2;
            y = Math.max(y - velocidad, 0);
        } else if (movingDown) {
            personaje = abajo;
            direccionActual = -2;
            y = Math.min(y + velocidad, ALTO_VENTANA - ALTO_PERSONAJE);
        }
        
        // Si no se está moviendo, asegurar que el personaje esté "quieto" (en caso de que 
        // una tecla se haya soltado mientras otra seguía presionada)
        if (!movingRight && !movingLeft && !movingUp && !movingDown) {
             personaje = quieto;
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (jugadorVivo) {
            
            // LLAMADA CLAVE: Ejecuta el movimiento fluido
            moverPersonaje();
            
            // Actualizar posición de las balas del jugador
            Iterator<Bala> iteratorBalas = balas.iterator();
            while (iteratorBalas.hasNext()) {
                Bala bala = iteratorBalas.next();
                bala.mover();
                if (bala.fueraDePantalla()) {
                    iteratorBalas.remove();
                }
            }

            // Actualizar posición de los láseres del planeta
            Iterator<Laser> iteratorLasers = lasers.iterator();
            while (iteratorLasers.hasNext()) {
                Laser laser = iteratorLasers.next();
                laser.mover();
                if (laser.fueraDePantalla()) {
                    iteratorLasers.remove();
                }
            }
            
            // LÓGICA DE CARGA DE DISPARO (Glow)
            final int UMBRAL = TIEMPO_ENTRE_DISPAROS_PLANETA / 3;

            if (contadorDisparoPlaneta == TIEMPO_ENTRE_DISPAROS_PLANETA - UMBRAL && !planetas.isEmpty()) {
                planetaCargando = planetas.get(random.nextInt(planetas.size()));
            }

            actualizarLogicaPlanetas();
            verificarColisiones();
        }

        // Actualizar explosiones
        Iterator<Explosion> iteradorExplosiones = explosiones.iterator();
        while (iteradorExplosiones.hasNext()) {
            Explosion explosion = iteradorExplosiones.next();
            explosion.duracion--;
            if (explosion.duracion <= 0) {
                iteradorExplosiones.remove();
            }
        }

        repaint();
    }

    //---------------------------------------------------------
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Dibujar el fondo
        if (fondo != null) {
            g.drawImage(fondo, 0, 0, ANCHO_VENTANA, ALTO_VENTANA, this);
        } else {
            g.setColor(new Color(135, 206, 235));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // Dibujar todos los planetas
        for (Planeta planeta : planetas) {
            if (imagenPlaneta != null) {
                g.drawImage(imagenPlaneta, planeta.x, planeta.y, ANCHO_PLANETA, ALTO_PLANETA, this);

                // Efecto de carga (rojo)
                if (planeta == planetaCargando) {
                    final int UMBRAL = TIEMPO_ENTRE_DISPAROS_PLANETA / 3;
                    int tiempoRestante = TIEMPO_ENTRE_DISPAROS_PLANETA - contadorDisparoPlaneta;
                    
                    if (tiempoRestante <= UMBRAL && tiempoRestante > 0) {
                        float factor = (float) tiempoRestante / UMBRAL;
                        float alpha = 0.2f + 0.6f * (1.0f - factor); 
                        
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                        g2d.setColor(Color.RED); 
                        g2d.fillOval(planeta.x, planeta.y, ANCHO_PLANETA, ALTO_PLANETA);
                        g2d.setComposite(AlphaComposite.SrcOver); 
                    }
                }

            } else {
                g.setColor(Color.BLUE);
                g.fillOval(planeta.x, planeta.y, ANCHO_PLANETA, ALTO_PLANETA);
            }
        }

        // Dibujar todas las balas del jugador
        for (Bala bala : balas) {
            if (imagenBala != null) {
                g.drawImage(imagenBala, bala.x, bala.y, ANCHO_BALA, ALTO_BALA, this);
            } else {
                g.setColor(Color.YELLOW);
                g.fillOval(bala.x, bala.y, ANCHO_BALA, ALTO_BALA);
            }
        }

        // Dibujar todos los láseres (ROTA LA IMAGEN HACIA EL JUGADOR)
        for (Laser l : lasers) {
            if (laser != null) {
                AffineTransform originalTransform = g2d.getTransform();

                // 1. Calcular el ángulo de rotación (en radianes)
                double angle = Math.atan2(l.velY, l.velX); 

                // 2. Calcular el centro de rotación (centro del láser)
                double centerX = l.x + ANCHO_LASER / 2.0;
                double centerY = l.y + ALTO_LASER / 2.0;

                // 3. Aplicar la rotación
                g2d.rotate(angle, centerX, centerY);

                // 4. Dibujar la imagen
                g2d.drawImage(laser, (int)l.x, (int)l.y, ANCHO_LASER, ALTO_LASER, this);

                // 5. Restaurar transformación original
                g2d.setTransform(originalTransform);

            } else {
                // Si no hay imagen, dibujar un rectángulo rojo (simple)
                g.setColor(Color.RED);
                g.fillRect((int)l.x, (int)l.y, ANCHO_LASER, ALTO_LASER);
            }
        }


        // Dibujar todas las explosiones
        for (Explosion explosion : explosiones) {
            if (imagenExplosion != null) {
                g.drawImage(imagenExplosion, explosion.x, explosion.y, ANCHO_PLANETA, ALTO_PLANETA, this);
            } else {
                g.setColor(Color.RED);
                g.fillOval(explosion.x, explosion.y, ANCHO_PLANETA, ALTO_PLANETA);
            }
        }

        // Dibujar el personaje SOLO si está vivo
        if (jugadorVivo) {
            if (personaje != null) {
                g.drawImage(personaje, x, y, ANCHO_PERSONAJE, ALTO_PERSONAJE, this);
            } else {
                g.setColor(Color.RED);
                g.fillRect(x, y, ANCHO_PERSONAJE, ALTO_PERSONAJE);
            }
        }

        // Dibujar contador de planetas destruidos
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Planetas destruidos: " + planetasDestruidos, 20, 30);

        // Dibujar mensaje de "Moriste"
        if (!jugadorVivo) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 100));
            String mensaje = "¡MORISTE!";
            FontMetrics fm = g.getFontMetrics();
            int msgX = (ANCHO_VENTANA - fm.stringWidth(mensaje)) / 2;
            int msgY = ALTO_VENTANA / 2;
            g.drawString(mensaje, msgX, msgY);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            String reiniciar = "Presiona ENTER para reiniciar";
            fm = g.getFontMetrics();
            msgX = (ANCHO_VENTANA - fm.stringWidth(reiniciar)) / 2;
            msgY += 50;
            g.drawString(reiniciar, msgX, msgY);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame ventana = new JFrame("System Civilian"
            		+ "");
            MarioBros juego = new MarioBros();

            ventana.add(juego);
            ventana.pack();
            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ventana.setLocationRelativeTo(null);
            ventana.setResizable(false);
            ventana.setVisible(true);

            juego.requestFocusInWindow();
        });
    }
}