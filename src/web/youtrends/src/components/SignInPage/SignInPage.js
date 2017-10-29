import React, {Component} from 'react';
import {Link} from 'react-router-dom';
import {FormGroup, Image, FormControl, Button, HelpBlock} from 'react-bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import './SignInPage.css';

class SignInPage extends Component {

	constructor () {
		super();
		this.state = {
			login: '',
			password: '',
			isAuthFailed: false
		};

		this.handleChangeLogin = this.handleChangeLogin.bind(this);
		this.handleChangePassword = this.handleChangePassword.bind(this);
		this.onSubmitClick = this.onSubmitClick.bind(this);

		if (localStorage.getItem('login')) {
			window.location.href = '/account';
		}
	}

	handleChangeLogin (e) {
		this.setState({login: e.target.value});
	}

	handleChangePassword (e) {
		this.setState({password: e.target.value});
	}

	getLoginValidationState () {
		const length = this.state.login.length;
		if (length > 3) return 'success';
		else if (length > 0) return 'error';
		return null;
	}

	getPasswordValidationState () {
		const length = this.state.password.length;
		if (length > 9) return 'success';
		else if (length > 0) return 'error';
		return null;
	}

	onSubmitClick () {
		fetch('/api/login', {
			method: 'POST',
			headers: {
				'Accept': 'application/json',
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({
														 login: this.state.login,
														 password: this.state.password,
													 })
		}).then((response) => {
			response.json().then((jsonResponse) => {
				if (jsonResponse) {
					// Super not secure, but for first realisation - norm
					localStorage.setItem("login", this.state.login);
					localStorage.setItem("password", this.state.password);
					window.location.href = '/account';
					this.setState({isAuthFailed: false});
				} else {
					this.setState({isAuthFailed: true});
				}
			});
		})
			.catch((error) => {
				console.error(error);
			});
	}

	render () {
		return (
			<form className={'form'}>
				<Image src="/media/logo.png" responsive/>
				<FormGroup
					controlId="formBasicText"
					validationState={this.getLoginValidationState()}
				>
					<FormControl
						type="text"
						placeholder="You chatId"
						onChange={this.handleChangeLogin}
					/>
				</FormGroup>
				<FormGroup
					controlId="formBasicText"
					validationState={this.getPasswordValidationState()}
				>
					<FormControl
						type="password"
						placeholder="You password"
						onChange={this.handleChangePassword}
					/>
				</FormGroup>
				<Button type="button"
								className={'width100'}
								onClick={this.onSubmitClick}
				>
					LogIn
				</Button>
				{
					this.state.isAuthFailed ?
						<HelpBlock className={'authFailedBlock'}>Login or password invalid</HelpBlock> : null
				}
				<div className={'center'}>
					<Link to={'/help'}>Where i can register?</Link>
				</div>
			</form>
		);
	}

}

export default SignInPage;
