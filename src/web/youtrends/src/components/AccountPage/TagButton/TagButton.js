import React, {Component} from 'react';
import {ButtonGroup, Button} from 'react-bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import './TagButton.css';

class TagButton extends Component {

	constructor () {
		super();
		this.onClick = this.onClick.bind(this);
	}

	onClick()
	{
		this.props.onClick(this.props.text);
	}


	render () {
		return (
			<ButtonGroup className={'textButton'}>
				<Button>{this.props.text}</Button>
				<Button onClick={this.onClick} className={'closeButton'}>X</Button>
			</ButtonGroup>
		);
	}

}

export default TagButton;
