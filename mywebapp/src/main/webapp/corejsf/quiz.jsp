<html>
   <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
   <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
   
   <f:view>
      <head>
      	<link href="styles.css" rel="styleSheet" type="text/css"/>
         <title><h:outputText value="#{msgs.title}"/></title>
      </head>
<script type="text/javascript"><!--
function checkAnswer(form) {
	// var answer = form['quizform:answer'].value;
	var answer = document.getElementById('quizform:answer');
	if (answer.value == null || answer.value == '') {
		alert("You must enter an answer");
		answer.focus();
		return false;
	}
	else {
		return true;
	}
}
	
function formValidator(){
	// Make quick references to our fields
	var firstname = document.getElementById('firstname');
	var addr = document.getElementById('addr');
	var zip = document.getElementById('zip');
	var state = document.getElementById('state');
	var username = document.getElementById('username');
	var email = document.getElementById('email');
	
	// Check each input in the order that it appears in the form!
	if(isAlphabet(firstname, "Please enter only letters for your name")){
		if(isAlphanumeric(addr, "Numbers and Letters Only for Address")){
			if(isNumeric(zip, "Please enter a valid zip code")){
				if(madeSelection(state, "Please Choose a State")){
					if(lengthRestriction(username, 6, 8)){
						if(emailValidator(email, "Please enter a valid email address")){
							return true;
						}
					}
				}
			}
		}
	}
	return false;
}

function isEmpty(elem, helperMsg){
	if(elem.value.length == 0){
		alert(helperMsg);
		elem.focus(); // set the focus to this input
		return true;
	}
	return false;
}

function isNumeric(elem, helperMsg){
	var numericExpression = /^[0-9]+$/;
	if(elem.value.match(numericExpression)){
		return true;
	}else{
		alert(helperMsg);
		elem.focus();
		return false;
	}
}

function isAlphabet(elem, helperMsg){
	var alphaExp = /^[a-zA-Z]+$/;
	if(elem.value.match(alphaExp)){
		return true;
	}else{
		alert(helperMsg);
		elem.focus();
		return false;
	}
}

function isAlphanumeric(elem, helperMsg){
	var alphaExp = /^[0-9a-zA-Z]+$/;
	if(elem.value.match(alphaExp)){
		return true;
	}else{
		alert(helperMsg);
		elem.focus();
		return false;
	}
}

function lengthRestriction(elem, min, max){
	var uInput = elem.value;
	if(uInput.length >= min && uInput.length <= max){
		return true;
	}else{
		alert("Please enter between " +min+ " and " +max+ " characters");
		elem.focus();
		return false;
	}
}

function madeSelection(elem, helperMsg){
	if(elem.value == "Please Choose"){
		alert(helperMsg);
		elem.focus();
		return false;
	}else{
		return true;
	}
}

function emailValidator(elem, helperMsg){
	var emailExp = /^[\w\-\.\+]+\@[a-zA-Z0-9\.\-]+\.[a-zA-z0-9]{2,4}$/;
	if(elem.value.match(emailExp)){
		return true;
	}else{
		alert(helperMsg);
		elem.focus();
		return false;
	}
}
//--></script>
      <body onload="document.getElementById('quizform:answer').focus();">
         <h:form id="quizform" onsubmit="return checkAnswer(this.form);">
         <h:messages errorClass="errors" layout="list" rendered="#{debug.showMessages}"/>
            <h3>
               <h:outputText value="#{msgs.heading}"/>
            </h3>
            <p>
               <h:outputFormat value="#{msgs.currentScore}">
                  <f:param value="#{quiz.score}"/>
               </h:outputFormat>
            </p>
            <p>
               <h:outputText value="#{msgs.guessNext}"/>
            </p>
            <p>
               <h:outputText value="#{quiz.current.sequence}" styleClass="prompts"/>
            </p>
            <p>
               <h:outputText value="#{msgs.answer}"/> 
               <h:inputText id="answer" value="#{quiz.answer}" required="true"/></p>
               <h:message for="answer" />
            <p>
               <h:commandButton value="#{msgs.next}" action="next"/>
            </p>
         </h:form>
	</body>
   </f:view>
</html>
