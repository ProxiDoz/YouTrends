import React, {Component} from 'react';
import {FormControl, Button} from 'react-bootstrap';
import TagButton from '../TagButton/TagButton';
import 'bootstrap/dist/css/bootstrap.css';
import './TagsControl.css';
import lodash from 'lodash';

class TagsControl extends Component {

	constructor () {
		super();
		this.state = {
			userSettingsData: null,
			textNewTag: null
		};

		this.handleChangeTextNewTag = this.handleChangeTextNewTag.bind(this);
		this.onAddNewTagClick = this.onAddNewTagClick.bind(this);
		this.removeTag = this.removeTag.bind(this);
	}

	componentDidMount () {
		fetch('http://localhost:8080/getUserSettingData', {
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

	onAddNewTagClick () {

		this.state.userSettingsData.bannedTags.push(this.state.textNewTag);

		fetch('http://localhost:8080/setUserSettingData', {
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

	removeTag (tagName) {

		let userSettingsData = lodash.cloneDeep(this.state.userSettingsData);

		let indexForDelete = userSettingsData.bannedTags.indexOf(tagName);

		delete userSettingsData.bannedTags[indexForDelete];

		fetch('http://localhost:8080/setUserSettingData', {
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
				<div className={'newTagInputBlock'}>
					<FormControl
						type="text"
						value={this.state.value}
						placeholder="Tag"
						onChange={this.handleChangeTextNewTag}
					/>
					<Button type="button"
									onClick={this.onAddNewTagClick}>Add</Button>
				</div>
				{
					userSettingsData.bannedTags.map((item, index) => (
						<TagButton onClick={this.removeTag.bind(this)} text={item} key={index}/>
					))
				}
			</div>
		);
	}

}

export default TagsControl;
