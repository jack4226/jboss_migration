package jpa.msgui.listener;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PhaseTracker implements PhaseListener {
	private static final long serialVersionUID = -3038658162634504399L;
	private static final String PHASE_PARAMETER = "listener.phaseTracker.phase";
	static final Logger logger = LogManager.getLogger(PhaseTracker.class);
	private String phase = null;

	public void setPhase(String newValue) {
		phase = newValue;
	}

	public PhaseId getPhaseId() {
		if (phase == null) {
			FacesContext context = FacesContext.getCurrentInstance();
			phase = (String) context.getExternalContext().getInitParameter(PHASE_PARAMETER);
		}
		PhaseId phaseId = PhaseId.ANY_PHASE;

		if (phase != null) {
			if ("RESTORE_VIEW".equals(phase))
				phaseId = PhaseId.RESTORE_VIEW;
			else if ("APPLY_REQUEST_VALUES".equals(phase))
				phaseId = PhaseId.APPLY_REQUEST_VALUES;
			else if ("PROCESS_VALIDATIONS".equals(phase))
				phaseId = PhaseId.PROCESS_VALIDATIONS;
			else if ("UPDATE_MODEL_VALUES".equals(phase))
				phaseId = PhaseId.UPDATE_MODEL_VALUES;
			else if ("INVOKE_APPLICATION".equals(phase))
				phaseId = PhaseId.INVOKE_APPLICATION;
			else if ("RENDER_RESPONSE".equals(phase))
				phaseId = PhaseId.RENDER_RESPONSE;
			else if ("ANY_PHASE".equals(phase))
				phaseId = PhaseId.ANY_PHASE;
		}
		return phaseId;
	}

	public void beforePhase(PhaseEvent e) {
		//logger.info("BEFORE " + e.getPhaseId());
		System.out.println("PhaseListener.BEFORE " + e.getPhaseId());
	}

	public void afterPhase(PhaseEvent e) {
		//logger.info("AFTER " + e.getPhaseId());
		System.out.println("PhaseListener.AFTER " + e.getPhaseId());
	}
}
