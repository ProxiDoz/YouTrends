import React, {Component} from 'react';
import {Nav, NavItem} from 'react-bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import './AccountPage.css';
import ChannelsControl from "./ChannelsControl/ChannelsControl";
import TagsControl from "./TagsControl/TagsControl";

class AccountPage extends Component {

	constructor () {
		super();
		this.state = {control: 'channels'};
		this.setChannelsControlVisible = this.setChannelsControlVisible.bind(this);
		this.setTagsControlVisible = this.setTagsControlVisible.bind(this);

	}

	setChannelsControlVisible () {
		this.setState({control: 'channels'});
	}

	setTagsControlVisible () {
		this.setState({control: 'tags'});
	}

	render () {
		let currentControl = <ChannelsControl/>;
		if (this.state.control === 'channels') {
			currentControl = <ChannelsControl/>
		}
		if (this.state.control === 'tags') {
			currentControl = <TagsControl/>
		}

		return (
			<div>
				<Nav bsStyle="tabs" activeKey={this.state.control} onSelect={this.handleSelect}>
					<NavItem eventKey="channels" onClick={this.setChannelsControlVisible}>Channels</NavItem>
					<NavItem eventKey="tags" onClick={this.setTagsControlVisible}>Tags</NavItem>
				</Nav>
				<div>
					{currentControl}
				</div>
			</div>
		);
	}

}

export default AccountPage;
