import React, {Component} from 'react';
import { Switch, Route } from 'react-router-dom';
import AccountPage from './components/AccountPage/AccountPage';
import SignInPage from './components/SignInPage/SignInPage';
import HelpPage from './components/HelpPage/HelpPage';
import './App.css';

class App extends Component {

	render () {
		return (

			<Switch>
				<Route exact path='/' component={SignInPage}/>
				<Route path='/help' component={HelpPage}/>
				<Route path='/sign' component={SignInPage}/>
				<Route path='/account' component={AccountPage}/>
			</Switch>

		);
	}
}

export default App;
