import React, { Component, useState } from 'react';
import PollList from '../../poll/PollList';
import { getUserProfile, changeAvatar } from '../../util/APIUtils';
import { Avatar, Tabs, notification, Button } from 'antd';
import { getAvatarColor } from '../../util/Colors';
import { formatDate } from '../../util/Helpers';
import LoadingIndicator from '../../common/LoadingIndicator';
import './Profile.css';
import NotFound from '../../common/NotFound';
import ServerError from '../../common/ServerError';
import { Upload } from 'antd';
import ImgCrop from 'antd-img-crop';
import 'antd/es/modal/style';
import 'antd/es/slider/style';
const TabPane = Tabs.TabPane;
class Profile extends Component {
    constructor(props) {
        super(props);
        this.state = {
            user: null,
            urlImage: "",
            isLoading: false,
            uploading: false,
            fileResponse: [],
            fileList: []
        }
        this.loadUserProfile = this.loadUserProfile.bind(this);
    }
    beforeUpload = async file => {
        this.setState(state => ({
            fileList: [...state.fileList, file],
        }));
        const formData = new FormData();
        formData.append("file", this.state.fileList[0]);
        console.log(this.state.fileList);
        changeAvatar(formData)
            .then(res => {
                console.log(res.data);
                alert("File uploaded successfully.")
            });
        this.setState({
            uploading: true,
        });
        window.location = window.location.href;
        return false;
    };

    loadUserProfile(username) {
        this.setState({
            isLoading: true
        });

        getUserProfile(username)
            .then(response => {
                this.setState({
                    user: response,
                    isLoading: false
                });
            }).catch(error => {
                if (error.status === 404) {
                    this.setState({
                        notFound: true,
                        isLoading: false
                    });
                } else {
                    this.setState({
                        serverError: true,
                        isLoading: false
                    });
                }
            });
    }

    componentDidMount() {
        const username = this.props.match.params.username;
        this.loadUserProfile(username);
    }

    componentDidUpdate(nextProps) {
        if (this.props.match.params.username !== nextProps.match.params.username) {
            this.loadUserProfile(nextProps.match.params.username);
        }
    }

    render() {
        if (this.state.isLoading) {
            return <LoadingIndicator />;
        }

        if (this.state.notFound) {
            return <NotFound />;
        }

        if (this.state.serverError) {
            return <ServerError />;
        }

        const tabBarStyle = {
            textAlign: 'center'
        };
        return (
            <div className="profile">
                {
                    this.state.user ? (
                        <div className="user-profile">
                            <div className="user-details">
                                <div className="user-avatar">
                                <ImgCrop rotate>
                                    <Upload
                                        // listType="picture-card"
                                        fileList={this.state.fileList}
                                        beforeUpload={this.beforeUpload}
                                    >
                                        {this.state.user.photo==null?
                                (<Avatar className="user-avatar-circle"
                                    style={{ backgroundColor: getAvatarColor(this.state.user.name) }} >
                                    {this.state.user.name[0].toUpperCase()}
                                </Avatar>):(
                                <Avatar className="user-avatar-circle" src={"http://localhost:5000/api/file/getImage/" + this.state.user.photo} />)}
                                    {/* <Avatar className="user-avatar-circle" src={"http://localhost:5000/api/file/getImage/" + this.state.user.photo} /> */}
                                    {/* <Avatar className="user-avatar-circle" style={{ backgroundColor: getAvatarColor(this.state.user.name)}}>
                                        {this.state.user.name[0].toUpperCase()}
                                    </Avatar> */}
                                    </Upload>
                                </ImgCrop>
                                </div>
                                {/* <div>
                                </div> */}
                                
                                    


                                <div className="user-summary">
                                    <div className="full-name">{this.state.user.name}</div>
                                    <div className="username">@{this.state.user.username}</div>
                                    <div className="user-joined">
                                        Joined {formatDate(this.state.user.joinedAt)}
                                    </div>
                                </div>
                            </div>
                            <div className="user-poll-details">
                                <Tabs defaultActiveKey="1"
                                    animated={false}
                                    tabBarStyle={tabBarStyle}
                                    size="large"
                                    className="profile-tabs">
                                    <TabPane tab={`${this.state.user.pollCount} Polls`} key="1">
                                        <PollList currentUser={this.props.currentUser} username={this.props.match.params.username} type="USER_CREATED_POLLS" isAuthenticated={this.props.isAuthenticated}/>
                                    </TabPane>
                                    <TabPane tab={`${this.state.user.voteCount} Votes`} key="2">
                                        <PollList currentUser={this.props.currentUser} username={this.props.match.params.username} type="USER_VOTED_POLLS" isAuthenticated={this.props.isAuthenticated}/>
                                    </TabPane>
                                </Tabs>
                            </div>
                        </div>
                    ) : null
                }
            </div>
        );
    }
}

export default Profile;