function validateInputs(myform) {
	var firstName = document.getElementById('firstName');
	if (firstName.value <= '') {
		alert("First name must be entered.");
		firstName.focus();
		return false;
	}
	
	var lastName = document.getElementById('lastName');
	if (lastName.value <= '') {
		alert("Last name must be entered.");
		lastName.focus();
		return false;
	}
	
	var emailAddr = document.getElementById('emailAddr');
	var regex = /^([a-z0-9\.\_\%\+\-])+\@([a-z0-9\-]+\.)+[a-z0-9]{2,4}$/i;
	if (!regex.test(emailAddr.value)) {
		alert('Please provide a valid email address');
		emailAddr.focus();
		return false;
	}
	
	var userPswd = document.getElementById('userPswd');
	if (userPswd.value <= '') {
		alert("Please enter your password.");
		userPswd.focus();
		return false;
	}
	
	var userPswd2 = document.getElementById('userPswd2');
	if (userPswd2.value <= '') {
		alert("Please confirm your password.");
		userPswd2.focus();
		return false;
	}
	
	if (userPswd.value != userPswd2.value) {
		alert("Passwords entered are different, please try again.");
		userPswd.focus();
		return false;
	}
	
	var countryCode = document.getElementById('countryCode');
	if (countryCode.value <= '') {
		alert("Please select a country.");
		countryCode.focus();
		return false;
	}
	
	var securityQuestion = document.getElementById('securityQuestion');
	if (securityQuestion.value <= '') {
		alert("Please select a security question.");
		securityQuestion.focus();
		return false;
	}
	
	var securityAnswer = document.getElementById('securityAnswer');
	if (securityAnswer.value <= '') {
		alert("Security answer must be entered.");
		securityAnswer.focus();
		return false;
	}
	
	return true;
}