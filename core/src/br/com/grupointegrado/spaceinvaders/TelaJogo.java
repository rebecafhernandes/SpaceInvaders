package br.com.grupointegrado.spaceinvaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FillViewport;


/**
 * Created by Rebeca on 03/08/2015.
 */
public class TelaJogo extends TelaBase {

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Stage palco;
    private BitmapFont fonte;
    private Label lbPontucao;
    private Label lbGameOver;
    private Image jogador;
    private Texture texturaJogador;
    private Texture texturaJogadorDireita;
    private Texture texturaJogadorEsquerda;
    private boolean indoDireita;
    private boolean indoEsquerda;
    private boolean atirando;
    private Array<Image> tiros = new Array<Image>();
    private Texture texturaTiro;
    private float intervaloTiros = 0; //Tempo acoumulado entre os tiros
    private final float MIN_INTERVALO_TIROS = 0.2f; //Mínimo de tempo entre tiros
    private Texture texturaMeteoro1;
    private Texture texturaMeteoro2;
    private Array<Image> meteoros1 = new Array<Image>();
    private Array<Image> meteoros2 = new Array<Image>();
    private Rectangle recJogador = new Rectangle();
    private Rectangle recTiro = new Rectangle();
    private Rectangle recMeteoro = new Rectangle();
    private int pontuacao = 0;
    private boolean gameOver = false;
    private Array<Texture> texturasExplosao = new Array<Texture>();
    private Array<Explosao> explosoes = new Array<Explosao>();
    private Stage palcoInformacoes;

    /**
     * Construtor padrão da tela de jogo
     * @param game Referência para a classe principal
     */
    public TelaJogo(MainGame game) {
        super(game);
    }

    /**
     * Método chamado quando a tela é exibida
     */
    @Override
    public void show() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();
        palco = new Stage(new FillViewport(camera.viewportWidth, camera.viewportHeight, camera));
        palcoInformacoes = new Stage(new FillViewport(camera.viewportWidth, camera.viewportHeight, camera));

        initTexturas();
        initFonte();
        initInformacoes();
        initJogador();
    }

    private void initTexturas() {
        texturaTiro =  new Texture("sprites/shot.png");
        texturaMeteoro1 = new Texture("sprites/enemie-1.png");
        texturaMeteoro2 = new Texture("sprites/enemie-2.png");

        for(int i = 1; i <= 17; i++) {
            Texture text = new Texture("sprites/explosion-" + i + ".png");
            texturasExplosao.add(text);
        }
    }

    /**
     * Instancia as informações escritas na tela
     */
    private void initInformacoes() {
        Label.LabelStyle lbEstilo = new Label.LabelStyle();
        lbEstilo.font = fonte;
        lbEstilo.fontColor = Color.WHITE;

        lbPontucao = new Label("0 pontos", lbEstilo);
        palcoInformacoes.addActor(lbPontucao);

        lbGameOver = new Label("Game Over!", lbEstilo);
        lbGameOver.setVisible(false);
        palcoInformacoes.addActor(lbGameOver);
    }

    /**
     * Instancia os objetos de fonte
     */
    private void initFonte() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/roboto.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.color = Color.WHITE;
        param.size = 24;
        param.shadowOffsetX = 2;
        param.shadowOffsetY = 2;
        param.shadowColor = Color.BLACK;

        //fonte = new BitmapFont();
        fonte = generator.generateFont(param);
        generator.dispose();
    }

    /**
     * Instancia os objetos do jogador e adiciona no palco
     */
    private void initJogador() {
        texturaJogador = new Texture("sprites/player.png");
        texturaJogadorDireita = new Texture("sprites/player-right.png");
        texturaJogadorEsquerda = new Texture("sprites/player-left.png");

        jogador = new Image(texturaJogador);
        float x = (camera.viewportWidth / 2) - (jogador.getWidth() / 2);
        float y = 15;
        jogador.setPosition(x, y);

        palco.addActor(jogador);
    }

    /**
     * Método chamado a todo quadro de atualização do jogo (FPS - frame per second)
     * @param delta Tempo entre um quadro e outro (em segundos)
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.15f, .15f, .25f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        lbPontucao.setPosition(10, camera.viewportHeight -  lbPontucao.getPrefHeight() - 10);
        lbPontucao.setText(pontuacao + " pontos");

        lbGameOver.setPosition(camera.viewportWidth / 2 - lbGameOver.getPrefWidth() / 2, camera.viewportHeight / 2);
        lbGameOver.setVisible(gameOver);

        atualizarExplosoes(delta);

        if (!gameOver) {
            capturaTeclas();
            atualizarJogador(delta);
            atualizarTiros(delta);
            atualizarMeteoros(delta);
            detectarColisoes(meteoros1, 5);
            detectarColisoes(meteoros2, 15);
        }

        //Atualiza a situação do palco
        palco.act(delta);

        //Desenha o palco na tela
        palco.draw();

        //Desenha o palco de informações na tela
        palcoInformacoes.act(delta);
        palcoInformacoes.draw();
    }

    private void atualizarExplosoes(float delta) {
        for (Explosao explosao : explosoes) {
            //Verifica se a explosão chegou ao fim
            if (explosao.getEstagio() >= 16) {
                explosoes.removeValue(explosao, true);
                explosao.getAtor().remove();
            } else {
                explosao.atualizar(delta);
            }
        }
    }

    private void detectarColisoes(Array<Image> meteoros, int valePonto) {
        recJogador.set(jogador.getX(), jogador.getY(), jogador.getWidth(), jogador.getHeight());

        for (Image meteoro : meteoros) {
            recMeteoro.set(meteoro.getX(), meteoro.getY(), meteoro.getWidth(), meteoro.getHeight());
            for (Image tiro: tiros) {
                recTiro.set(tiro.getX(), tiro.getY(), tiro.getWidth(), tiro.getHeight());

                if (recMeteoro.overlaps(recTiro)) {
                    //Se sim, ocorre uma colisão do tiro com o meteoro 1
                    pontuacao += valePonto;

                    tiro.remove();
                    tiros.removeValue(tiro, true);

                    meteoro.remove();
                    meteoros.removeValue(meteoro, true);

                    criarExplosao(meteoro.getX(), meteoro.getY());
                }
            }

            if (recMeteoro.overlaps(recJogador)) {
                gameOver = true;
            }
        }
    }

    private void criarExplosao(float x, float y) {
        Image ator = new Image(texturasExplosao.get(0));
        ator.setPosition(x, y);
        palco.addActor(ator);

        Explosao explosao = new Explosao(texturasExplosao, ator);
        explosoes.add(explosao);
    }

    private void atualizarMeteoros(float delta) {
        int qtdMeteoros = meteoros1.size + meteoros2.size;

        if (qtdMeteoros < 10) {
            int tipo = MathUtils.random(1, 4); //Retorna 1 ou 2 aleatoriamente

            if (tipo == 1) {
                //Cria meteoro 1
                Image meteoro = new Image(texturaMeteoro1);
                float x = MathUtils.random(0, camera.viewportWidth - meteoro.getWidth());
                float y = MathUtils.random(camera.viewportHeight, camera.viewportWidth * 2);
                meteoro.setPosition(x, y);
                meteoros1.add(meteoro);
                palco.addActor(meteoro);
            } else if (tipo == 2) {
                //Cria meteoro 2
                Image meteoro = new Image(texturaMeteoro2);
                float x = MathUtils.random(0, camera.viewportWidth - meteoro.getWidth());
                float y = MathUtils.random(camera.viewportHeight, camera.viewportWidth * 2);
                meteoro.setPosition(x, y);
                meteoros2.add(meteoro);
                palco.addActor(meteoro);
            }
        }

        float velocidade1 = 100; //200 pixels por segundo

        for (Image meteoro : meteoros1) {
            float x = meteoro.getX();
            float y = meteoro.getY() - velocidade1 * delta;
            meteoro.setPosition(x, y);

            //Remove os tiros que sairam da tela
            if (meteoro.getY() + meteoro.getHeight() < 0) {
                meteoros1.removeValue(meteoro, true); //Remove da lista
                meteoro.remove(); //Remove do palco
            }
        }

        float velocidade2 = 150; //200 pixels por segundo

        for (Image meteoro : meteoros2) {
            float x = meteoro.getX();
            float y = meteoro.getY() - velocidade2 * delta;
            meteoro.setPosition(x, y);

            //Remove os tiros que sairam da tela
            if (meteoro.getY() + meteoro.getHeight() < 0) {
                meteoros2.removeValue(meteoro, true); //Remove da lista
                meteoro.remove(); //Remove do palco
            }
        }
    }

    /**
     *
     * @param delta
     */
    private void atualizarTiros(float delta) {
        intervaloTiros = intervaloTiros + delta; //Acumula o tempo percorrido

        //Cria um novo tiro se necessário
        if (atirando) {
            //Verifica se o tempo mínimo foi atingido
            if (intervaloTiros >= MIN_INTERVALO_TIROS) {
                Image tiro = new Image(texturaTiro);
                float x = jogador.getX() + (jogador.getWidth() / 2 - tiro.getWidth() / 2);
                float y = jogador.getY() + jogador.getHeight();
                tiro.setPosition(x, y);
                tiros.add(tiro);
                palco.addActor(tiro);
                intervaloTiros = 0;
            }
        }

        float velocidade = 200; //Velocidade de movimentação do tiro

        //Percorre todos os tiros existentes
        for (Image tiro : tiros) {
            //Movimenta o tiro em direção ao topo
            float x = tiro.getX();
            float y = tiro.getY() + velocidade * delta;
            tiro.setPosition(x, y);

            //Remove os tiros que sairam da tela
            if (tiro.getY() > camera.viewportHeight) {
                tiros.removeValue(tiro, true); //Remove da lista
                tiro.remove(); //Remove do palco
            }
        }
    }

    /**
     * Atualiza a posição do jogador
     * @param delta
     */
    private void atualizarJogador(float delta) {
        //Velocidade do movimento do jogador
        float velocidade = 200;

        if (indoDireita) {
            //Verifica se o jogador está dentro da tela
            if (jogador.getX() < (camera.viewportWidth - jogador.getWidth())) {
                float x = jogador.getX() + velocidade * delta;
                float y = jogador.getY();
                jogador.setPosition(x, y);
            }
        }

        if (indoEsquerda) {
            //Verifica se o jogador está dentro da tela
            if (jogador.getX() > 0) {
                float x = jogador.getX() - velocidade * delta;
                float y = jogador.getY();
                jogador.setPosition(x, y);
            }
        }

        if (indoDireita) {
            //Trocar imagem player-right
            jogador.setDrawable(new SpriteDrawable(new Sprite(texturaJogadorDireita)));
        } else if (indoEsquerda) {
            //Troca imagem para player-left
            jogador.setDrawable(new SpriteDrawable(new Sprite(texturaJogadorEsquerda)));
        } else {
           //Troca imagem para player
            jogador.setDrawable(new SpriteDrawable(new Sprite(texturaJogador)));
        }
    }

    /**
     * Verifica se as teclas estão pressionadas
     */
    private void capturaTeclas() {
        indoDireita = false;
        indoEsquerda = false;
        atirando = false;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            indoDireita = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            indoEsquerda = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            atirando = true;
        }
    }

    /**
     * Método chamado sempre que há uma alteração no tamanho da tela
     * @param width Novo valor de largura da tela
     * @param height Novo valor de altura da tela
     */
    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        camera.update();
    }

    /**
     * Método chamado sempre que o jogo for minimizado
     */
    @Override
    public void pause() {

    }

    /**
     * Método chamado sempre que o jogo volta para o primeiro plano
     */
    @Override
    public void resume() {

    }

    /**
     * Método chamado quando a tela for destruída
     */
    @Override
    public void dispose() {
        batch.dispose();
        palco.dispose();
        fonte.dispose();
        palcoInformacoes.dispose();

        texturaJogador.dispose();
        texturaJogadorDireita.dispose();
        texturaJogadorEsquerda.dispose();
        texturaTiro.dispose();

        texturaMeteoro1.dispose();
        texturaMeteoro2.dispose();

        for(Texture text: texturasExplosao) {
            text.dispose();
        }
    }
}
