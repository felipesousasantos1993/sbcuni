package br.com.sbcuni.mensagem.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import br.com.sbcuni.bean.GenericBean;
import br.com.sbcuni.constantes.Constantes;
import br.com.sbcuni.constantes.Tela;
import br.com.sbcuni.mensagem.entity.Mensagem;
import br.com.sbcuni.mensagem.service.MensagemServiceBean;
import br.com.sbcuni.usuario.bean.UsuarioSessionBean;
import br.com.sbcuni.usuario.entity.Usuario;
import br.com.sbcuni.usuario.service.UsuarioServiceBean;
import br.com.sbcuni.util.Util;

@ManagedBean
@ViewScoped
public class CaixaEntradaBean extends GenericBean {

	private static final long serialVersionUID = 1894538838110348372L;

	private List<Mensagem> mensagemsRecebidas;
	private List<Mensagem> mensagemEnviadas;
	private List<Mensagem> mensagensExcluidas;
	private List<Usuario> listaAlunos;
	private List<String> destinatariosSelecionadas = new ArrayList<String>();
	
	@EJB
	private UsuarioServiceBean usuarioServiceBean;
	@EJB
	private MensagemServiceBean mensagemServiceBean;

	private Boolean msgRecebidas = Boolean.FALSE;
	private Boolean msgEnviadas = Boolean.FALSE;
	private Boolean msgExcluidas = Boolean.FALSE;
	private Boolean verMensagem = Boolean.FALSE;
	
	private Mensagem mensagem = new Mensagem();
	private Mensagem mensagemSelecionada = new Mensagem();
	
	private Integer nuMsgRecebidas;
	private Integer nuMsgEnviadas;
	private Integer nuMsgExcluidas;
	
	
	@PostConstruct
	public void init() {
		atualizarCaixaEntrada();
		msgRecebidas = Boolean.TRUE;
		listaAlunos = usuarioServiceBean.consultarPorPerfil(Constantes.PERFIL_ALUNO);
	}
	
	public void atualizarCaixaEntrada() {
		mensagemsRecebidas = mensagemServiceBean.consultarRecebidas(UsuarioSessionBean.getInstance().getUsuarioSessao());
		mensagemEnviadas = mensagemServiceBean.consultarEnviadas(UsuarioSessionBean.getInstance().getUsuarioSessao());
		mensagensExcluidas = mensagemServiceBean.consultarRecebidasLixeira(UsuarioSessionBean.getInstance().getUsuarioSessao());
	}
	
	public void selecionarMensagem(Mensagem mensagem) {
		setMensagemSelecionada(mensagem);
	}
	
	public String enviarMensagem() {
		List<Usuario> usuariosSelecionados = new ArrayList<Usuario>();
		for (String idUsuario : destinatariosSelecionadas) {
			usuariosSelecionados.add(usuarioServiceBean.consultarUsuarioPorId(Long.valueOf(idUsuario)));
		}
		mensagem.setDtEnvio(new Date());
		mensagem.setRemetente(UsuarioSessionBean.getInstance().getUsuarioSessao());
		try {
			mensagem.setTipo(Constantes.MSG_ENVIADA);
			mensagemServiceBean.enviarMensagem(mensagem);
			for (Usuario usuario : usuariosSelecionados) {
				Mensagem m = new Mensagem();
				m.setTipo(Constantes.MSG_PRINCIPAL);
				m.setDestinatario(usuario);
				m.setDtEnvio(mensagem.getDtEnvio());
				m.setTitulo(mensagem.getTitulo());
				m.setMensagem(mensagem.getMensagem());
				m.setRemetente(mensagem.getRemetente());
				mensagemServiceBean.enviarMensagem(m);
			}
			exibirMsgSucesso("Mensagem enviada com sucesso!");
			atualizarCaixaEntrada();
			mensagem = new Mensagem();
			usuariosSelecionados = new ArrayList<Usuario>();
			destinatariosSelecionadas = new ArrayList<String>();
			return Tela.CAIXA_ENTRADA;
		} catch (Exception e) {
			exibirMsgErro(e.getMessage());
			return null;
		}
	}
	
	public String atualizarMsg() {
		atualizarCaixaEntrada();
		return Tela.CAIXA_ENTRADA;
	}
	
	public String excluirMsg() {
		if (msgRecebidas) {
			excluirMsgSelecionadas(mensagemsRecebidas);
		} else if (msgEnviadas) {
			excluirMsgSelecionadas(mensagemEnviadas);
		}
		atualizarCaixaEntrada();
		return Tela.CAIXA_ENTRADA;
	}
	
	public String removerLixeiraMsgs() {
		removerLixeira(mensagensExcluidas);
		atualizarCaixaEntrada();
		return Tela.CAIXA_ENTRADA;
	}
	
	public void excluirMsgSelecionadas(List<Mensagem> list) {
		for (Mensagem mensagem : list) {
			if(!Util.isNull(mensagem.getSelecionada())) {
				if (mensagem.getSelecionada()) {
					mensagem.setTipo(Constantes.MSG_LIXEIRA);
					mensagemServiceBean.enviarMensagem(mensagem);
				}
			}
		}
	}
	public void removerLixeira(List<Mensagem> list) {
		for (Mensagem mensagem : list) {
			if(!Util.isNull(mensagem.getSelecionada())) {
				if (mensagem.getSelecionada()) {
					mensagem.setTipo(Constantes.MSG_PRINCIPAL);
					mensagemServiceBean.enviarMensagem(mensagem);
				}
			}
		}
	}
	
	public void selecionarMsg(Mensagem mensagem) {
		mensagem.setSelecionada(Boolean.TRUE);
	}
	
	public void selecionarTodasMsgs() {
		if (msgRecebidas) {
			selecionarTodasMensagem(mensagemsRecebidas);
		} else if (msgEnviadas) {
			selecionarTodasMensagem(mensagemEnviadas);
		} else if (msgExcluidas) {
			selecionarTodasMensagem(mensagensExcluidas);
		}
	}
	public void deselecionarTodasMsgs() {
		if (msgRecebidas) {
			deselecionarTodasMensagem(mensagemsRecebidas);
		} else if (msgEnviadas) {
			deselecionarTodasMensagem(mensagemEnviadas);
		} else if (msgExcluidas) {
			deselecionarTodasMensagem(mensagensExcluidas);
		}
	}
	
	// SELECIONAR
	public void selecionarTodasMensagem(List<Mensagem> lista) {
		for (Mensagem m : lista) {
			m.setSelecionada(Boolean.TRUE);
		}
	}
	public void deselecionarTodasMensagem(List<Mensagem> lista) {
		for (Mensagem m : lista) {
			m.setSelecionada(Boolean.FALSE);
		}
	}
	
	
	
	public void exibirMsgsRecebidas() {
		msgRecebidas = Boolean.TRUE;
		msgEnviadas = Boolean.FALSE;
		msgExcluidas = Boolean.FALSE;
	}
	
	public void exibirMsgsEnviadas() {
		msgRecebidas = Boolean.FALSE;
		msgEnviadas = Boolean.TRUE;
		msgExcluidas = Boolean.FALSE;
	}
	
	public void exibirMsgsExcluidas() {
		msgRecebidas = Boolean.FALSE;
		msgEnviadas = Boolean.FALSE;
		msgExcluidas = Boolean.TRUE;
	}


	public List<Mensagem> getMensagemsRecebidas() {
		return mensagemsRecebidas;
	}

	public void setMensagemsRecebidas(List<Mensagem> mensagemsRecebidas) {
		this.mensagemsRecebidas = mensagemsRecebidas;
	}

	public List<Mensagem> getMensagemEnviadas() {
		return mensagemEnviadas;
	}

	public void setMensagemEnviadas(List<Mensagem> mensagemEnviadas) {
		this.mensagemEnviadas = mensagemEnviadas;
	}


	public Boolean getMsgRecebidas() {
		return msgRecebidas;
	}


	public void setMsgRecebidas(Boolean msgRecebidas) {
		this.msgRecebidas = msgRecebidas;
	}


	public Boolean getMsgEnviadas() {
		return msgEnviadas;
	}


	public void setMsgEnviadas(Boolean msgEnviadas) {
		this.msgEnviadas = msgEnviadas;
	}


	public Boolean getMsgExcluidas() {
		return msgExcluidas;
	}


	public void setMsgExcluidas(Boolean msgExcluidas) {
		this.msgExcluidas = msgExcluidas;
	}


	public Integer getNuMsgRecebidas() {
		return getMensagemsRecebidas().size();
	}


	public void setNuMsgRecebidas(Integer nuMsgRecebidas) {
		this.nuMsgRecebidas = nuMsgRecebidas;
	}


	public Integer getNuMsgEnviadas() {
		return getMensagemEnviadas().size();
	}


	public void setNuMsgEnviadas(Integer nuMsgEnviadas) {
		this.nuMsgEnviadas = nuMsgEnviadas;
	}


	public Integer getNuMsgExcluidas() {
		return getMensagensExcluidas().size();
	}


	public void setNuMsgExcluidas(Integer nuMsgExcluidas) {
		this.nuMsgExcluidas = nuMsgExcluidas;
	}

	public List<Mensagem> getMensagensExcluidas() {
		return mensagensExcluidas;
	}

	public void setMensagensExcluidas(List<Mensagem> mensagensExcluidas) {
		this.mensagensExcluidas = mensagensExcluidas;
	}

	public Mensagem getMensagem() {
		return mensagem;
	}

	public void setMensagem(Mensagem mensagem) {
		this.mensagem = mensagem;
	}

	public List<Usuario> getListaAlunos() {
		return listaAlunos;
	}

	public void setListaAlunos(List<Usuario> listaAlunos) {
		this.listaAlunos = listaAlunos;
	}

	public List<String> getDestinatariosSelecionadas() {
		return destinatariosSelecionadas;
	}

	public void setDestinatariosSelecionadas(List<String> destinatariosSelecionadas) {
		this.destinatariosSelecionadas = destinatariosSelecionadas;
	}

	public Boolean getVerMensagem() {
		return verMensagem;
	}

	public void setVerMensagem(Boolean verMensagem) {
		this.verMensagem = verMensagem;
	}

	public Mensagem getMensagemSelecionada() {
		return mensagemSelecionada;
	}

	public void setMensagemSelecionada(Mensagem mensagemSelecionada) {
		this.mensagemSelecionada = mensagemSelecionada;
	}
	
}