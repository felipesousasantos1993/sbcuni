package br.com.sbcuni.grupoEstudo.bean;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import br.com.sbcuni.avaliacao.service.AvaliacaoServiceBean;
import br.com.sbcuni.bean.GenericBean;
import br.com.sbcuni.comentario.service.ComentarioServiceBean;
import br.com.sbcuni.constantes.Tela;
import br.com.sbcuni.exception.SbcuniException;
import br.com.sbcuni.grupoEstudo.GrupoEstudo;
import br.com.sbcuni.grupoEstudo.service.GrupoEstudoSerivceBean;
import br.com.sbcuni.topico.entity.Topico;
import br.com.sbcuni.topico.service.TopicoServiceBean;
import br.com.sbcuni.usuario.entity.Usuario;
import br.com.sbcuni.util.WebResources;

@ManagedBean
@ViewScoped
public class DetalheGrupoEstudoBean extends GenericBean {

	private static final long serialVersionUID = 1L;

	public DetalheGrupoEstudoBean() {
		super();
	}

	@EJB
	private AvaliacaoServiceBean avaliacaoServiceBean;
	@EJB
	private ComentarioServiceBean comentarioServiceBean;
	@EJB
	private TopicoServiceBean topicoServiceBean;
	@EJB
	private GrupoEstudoSerivceBean grupoEstudoSerivceBean;
	
	@PostConstruct
	public void init() {
		grupoEstudo = (GrupoEstudo) WebResources.getFlash().get(WebResources.GRUPO_ESTUDO);
		for (Usuario aluno : grupoEstudo.getAlunos()) {
			aluno.setTopicos(topicoServiceBean.buscarTopicoPorUsuario(aluno.getIdUsuario()));
			aluno.setNuComentariosNoGrupo(comentarioServiceBean.consultarNuComentariosUsuarioGrupoEstudo(aluno, grupoEstudo));
		}
		for (Topico t : grupoEstudo.getTopicosGrupo()) {
			avaliacaoServiceBean.definirAvaliacaoTopico(t);
			t.setComentarios(comentarioServiceBean.consultarComentariosTopico(t));
		}
	}
	
	private GrupoEstudo grupoEstudo;
	
	private Boolean incluirAluno = Boolean.FALSE;
 	
	public String telaNovoTopico() {
		WebResources.getFlash().put(WebResources.GRUPO_ESTUDO, grupoEstudo);
		return Tela.NOVO_TOPICO_GRUPO_ESTUDO;
	}
	
	public String telaVisualizarTopico(Topico topico) {
		return detalharTopico(topico);
	}
	
	public String telaIncluirAluno() {
		WebResources.getFlash().put(WebResources.GRUPO_ESTUDO, grupoEstudo);
		return Tela.INCLUIR_ALUNO_PATH;
	}
	
	public void exibirIncluirAlunos() {
		setIncluirAluno(Boolean.TRUE);
	}
	
	public String excluirAlunoGrupo(Usuario usuario) {
		grupoEstudo.getAlunos().remove(usuario);
		try {
			grupoEstudoSerivceBean.alterarGrupoEstudo(grupoEstudo);
			WebResources.getFlash().put(WebResources.GRUPO_ESTUDO, grupoEstudo);
			return Tela.DETALHE_GRUPO_ESTUDO_PATH;
		} catch (SbcuniException e) {
			exibirMsgErro(e.getMessage());
			return null;
		}
	}
	
	public void alteraNomeGrupo() {
		try {
			grupoEstudoSerivceBean.alterarGrupoEstudo(grupoEstudo);
		} catch (SbcuniException e) {
			exibirMsgAviso(e.getMessage());
		}
	}

	public void marcarAluno(Usuario usuario) {
		usuario.setMarcado(Boolean.TRUE);
	}
	public void desmarcarAluno(Usuario usuario) {
		usuario.setMarcado(Boolean.FALSE);
	}

	public GrupoEstudo getGrupoEstudo() {
		return grupoEstudo;
	}

	public void setGrupoEstudo(GrupoEstudo grupoEstudo) {
		this.grupoEstudo = grupoEstudo;
	}

	public Boolean getIncluirAluno() {
		return incluirAluno;
	}

	public void setIncluirAluno(Boolean incluirAluno) {
		this.incluirAluno = incluirAluno;
	}
	
}
