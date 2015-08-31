package br.com.grupointegrado.spaceinvaders;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Rebeca on 31/08/2015.
 */
public class Explosao {
    private static float tempoTroca = 1f / 17f;

    private int estagio = 0; //Controla o estágio de 0 a 16
    private Array<Texture> texturas;
    private Image ator;
    private float tempoAcumulado = 0;

    public Explosao(Array<Texture> texturas, Image ator) {
        this.texturas = texturas;
        this.ator = ator;
    }

    /**
     * Calcula o tempo acumulado e realiza a troca do estágio da explosao
     * Exemplo:
     * Cada quadro demora 0,016 segundos
     * Cada imagem deve permanecer 0,05 segundos
     * @param delta
     */
    public void atualizar(float delta) {
        tempoAcumulado += delta;

        if (tempoAcumulado >= tempoTroca) {
            tempoAcumulado = 0;
            estagio++;
            Texture textura = texturas.get(estagio);
            ator.setDrawable(new SpriteDrawable(new Sprite(textura)));
        }
    }

    public int getEstagio() {
        return estagio;
    }

    public void setEstagio(int estagio) {
        this.estagio = estagio;
    }

    public Array<Texture> getTexturas() {
        return texturas;
    }

    public void setTexturas(Array<Texture> texturas) {
        this.texturas = texturas;
    }

    public Image getAtor() {
        return ator;
    }

    public void setAtor(Image ator) {
        this.ator = ator;
    }
}
