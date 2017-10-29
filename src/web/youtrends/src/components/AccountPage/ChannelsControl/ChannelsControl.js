import React, {Component} from 'react';
import {FormControl, Button} from 'react-bootstrap';
import TagButton from '../TagButton/TagButton';
import 'bootstrap/dist/css/bootstrap.css';
import './ChannelsControl.css';
import lodash from 'lodash';

class ChannelsControl extends Component {

	constructor () {
		super();
		this.state = {
			userSettingsData: null,
			textNewTag: null
		};

		this.handleChangeTextNewTag = this.handleChangeTextNewTag.bind(this);
		this.onAddNewChannelClick = this.onAddNewChannelClick.bind(this);
		this.removeChannel = this.removeChannel.bind(this);
	}

	componentDidMount () {
		fetch('/api/getUserSettingData', {
			method: 'POST',
			headers: {
				'Accept': 'application/json',
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({
														 login: localStorage.getItem('login'),
														 password: localStorage.getItem('password'),
													 })
		}).then((response) => {
			response.json().then((jsonResponse) => {
				this.setState({userSettingsData: jsonResponse});
			});
		})
			.catch((error) => {
				console.error(error);
			});
	}

	handleChangeTextNewTag (e) {
		this.setState({textNewTag: e.target.value});
	}

	onAddNewChannelClick () {

		this.state.userSettingsData.bannedChannels.push(this.state.textNewTag);

		fetch('/api/setUserSettingData', {
			method: 'POST',
			headers: {
				'Accept': 'application/json',
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({
														 credentials: {
															 login: localStorage.getItem('login'),
															 password: localStorage.getItem('password')
														 },
														 bannedChannels: this.state.userSettingsData.bannedChannels,
														 bannedTags: this.state.userSettingsData.bannedTags
													 })
		}).then((response) => {
			response.json().then((jsonResponse) => {
				this.setState({userSettingsData: jsonResponse});
			});
		})
			.catch((error) => {
				console.error(error);
			});
	}

	removeChannel (channelName) {

		let userSettingsData = lodash.cloneDeep(this.state.userSettingsData);

		let indexForDelete = userSettingsData.bannedChannels.indexOf(channelName);

		delete userSettingsData.bannedChannels[indexForDelete];

		fetch('/api/setUserSettingData', {
			method: 'POST',
			headers: {
				'Accept': 'application/json',
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({
														 credentials: {
															 login: localStorage.getItem('login'),
															 password: localStorage.getItem('password')
														 },
														 bannedChannels: userSettingsData.bannedChannels,
														 bannedTags: userSettingsData.bannedTags
													 })
		}).then((response) => {
			response.json().then((jsonResponse) => {
				this.setState({userSettingsData: jsonResponse});
			});
		})
			.catch((error) => {
				console.error(error);
			});
	}

	render () {
		const userSettingsData = this.state.userSettingsData;
		if (!userSettingsData) return <div>Loading</div>;
		return (
			<div>
				<div className={'newChannelInputBlock'}>
					<FormControl
						type="text"
						value={this.state.value}
						placeholder="Channel"
						onChange={this.handleChangeTextNewTag}
					/>
					<Button type="button"
									onClick={this.onAddNewChannelClick}>Add</Button>
				</div>
				{
					userSettingsData.bannedChannels.map((item, index) => (
						<TagButton onClick={this.removeChannel.bind(this)} text={item} key={index}/>
					))
				}
			</div>
		);
	}

}

export default ChannelsControl;
