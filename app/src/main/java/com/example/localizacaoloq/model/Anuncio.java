package com.example.localizacaoloq.model;
import java.util.Date;
import java.util.List;

public class Anuncio {
    private String _id;
    private String titulo;
    private String mensagem;
    private Local local;
    private List<ListaChave> listaChave;
    private User user;
    private String modoEntrega;
    private String politica;
    private Date inicio;
    private Date fim;
    private Date createdAt;
    private Date updatedAt;
    public Anuncio() {}
    public Anuncio(String titulo, String mensagem, Local local,
                   List<ListaChave> listaChave, User user,
                   String modoEntrega, String politica, Date inicio, Date fim) {
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.local = local;
        this.listaChave = listaChave;  // ADICIONADO
        this.user = user;
        this.modoEntrega = modoEntrega;
        this.politica = politica;
        this.inicio = inicio;
        this.fim = fim;
    }

    // Getters e Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    // ADICIONADO: Getter e Setter de listaChave
    public List<ListaChave> getListaChave() {
        return listaChave;
    }

    public void setListaChave(List<ListaChave> listaChave) {
        this.listaChave = listaChave;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getModoEntrega() {
        return modoEntrega;
    }

    public void setModoEntrega(String modoEntrega) {
        this.modoEntrega = modoEntrega;
    }

    public String getPolitica() {
        return politica;
    }

    public void setPolitica(String politica) {
        this.politica = politica;
    }

    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Anuncio{" +
                "_id='" + _id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", local=" + (local != null ? local.getNome() : "null") +
                ", listaChave=" + (listaChave != null ? listaChave.size() + " restrições" : "0 restrições") +
                ", politica='" + politica + '\'' +
                ", modoEntrega='" + modoEntrega + '\'' +
                '}';
    }
}