
import React, { Component } from 'react';
import './Poll.css';
import { Statistic,Avatar, Card, Col, Row, Badge, notification, Input, Modal } from 'antd';
import { MAX_CHOICES } from '../constants';
import { addChoice } from '../util/APIUtils';
import { Link } from 'react-router-dom';
import { getAvatarColor } from '../util/Colors';
import { formatDateTime } from '../util/Helpers';
import {
    CheckCircleOutlined, CloseOutlined, PlusOutlined, DeleteOutlined,ExclamationCircleOutlined
} from '@ant-design/icons';
import { Radio, Button } from 'antd';
const RadioGroup = Radio.Group;
const { Countdown } = Statistic;
class Poll extends Component {
    constructor(props) {
        super(props);
        this.state = {
            choiceAdd: null,
            choiceCanAdd: this.props.choiceCanAdd,
            choiceNew: {
                text: ''
            }
        }
    }
    calculatePercentage = (choice) => {
        if (this.props.poll.totalVotes === 0) {
            return 0;
        }
        return (choice.voteCount * 100) / (this.props.poll.totalVotes);
    };

    isSelected = (choice) => {
        // var a = false;
        // // this.props.poll.selectedChoice.forEach(element => {
        // //     if (element == choice.id) {
        // //         a = true;
        // //     }
        // // });
        // return a;
        return this.props.poll.selectedChoice === choice.id;
    }
    getWinningChoice = () => {
        return this.props.poll.choices.reduce((prevChoice, currentChoice) =>
            currentChoice.voteCount > prevChoice.voteCount ? currentChoice : prevChoice,
            { voteCount: -Infinity }
        );
    }
    onChange = (event) => {
        const choiceA = {
            text: event.target.value
        };
        this.setState({
            choiceNew: choiceA
        });

    }

    addChoiceInput = () => {
        this.setState(
            {
                choiceAdd: <Input
                    placeholder='Choice '
                    size="large"
                    defaultValue={this.state.choiceNew.text}
                    className="cv-poll-choice"
                    onPressEnter={this.props.addAChoice}
                    onChange={this.onChange}
                />,
                choiceCanAdd: false
            }
        )
    }
    getTimeCountDown = (poll) => {
        const expirationTime = new Date(poll.expirationDateTime).getTime();
        return expirationTime;
    }
    getTimeRemaining = (poll) => {
        const expirationTime = new Date(poll.expirationDateTime).getTime();
        const currentTime = new Date().getTime();

        var difference_ms = expirationTime - currentTime;
        var seconds = Math.floor((difference_ms / 1000) % 60);
        var minutes = Math.floor((difference_ms / 1000 / 60) % 60);
        var hours = Math.floor((difference_ms / (1000 * 60 * 60)) % 24);
        var days = Math.floor(difference_ms / (1000 * 60 * 60 * 24));

        let timeRemaining;

        if (days > 0) {
            timeRemaining = days + " days left";
        } else if (hours > 0) {
            timeRemaining = hours + " hours left";
        } else if (minutes > 0) {
            timeRemaining = minutes + " minutes left";
        } else if (seconds > 0) {
            timeRemaining = seconds + " seconds left";
        } else {
            timeRemaining = "less than a second left";
        }

        return timeRemaining;
    }
    render() {
        const pollChoices = [];
        if (this.props.poll.selectedChoice && !this.props.poll.expired) {
            const winningChoice = this.props.poll.expired ? this.getWinningChoice() : null;

            this.props.poll.choices.forEach(choice => {
                pollChoices.push(
                    pollChoices.push(<CompletedOrVotedCanVotePollChoice
                        key={choice.id}
                        choice={choice}
                        isSelected={this.isSelected(choice)}
                        percentVote={this.calculatePercentage(choice)}
                        handleClick={this.props.handleDeleteVoteSubmit}
                        handleClickDelChoice={this.props.handleDeleteChoiceSubmit}
                        isDelete={this.props.currentUser.username===this.props.poll.createdBy.username&&!this.props.poll.expired}
                        canFix={this.props.poll.isCanFix}
                    />));
            });
        }
        else if (/* this.props.poll.selectedChoice ||  */this.props.poll.expired) {
            const winningChoice = this.props.poll.expired ? this.getWinningChoice() : null;

            this.props.poll.choices.forEach(choice => {
                pollChoices.push(<CompletedOrVotedPollChoice
                    key={choice.id}
                    choice={choice}
                    isWinner={winningChoice && choice.id === winningChoice.id}
                    isSelected={this.isSelected(choice)}
                    percentVote={this.calculatePercentage(choice)}
                />);
            });
        } else {
            this.props.poll.choices.forEach(choice => {
                pollChoices.push(
                    <Radio className="poll-choice-radio" key={choice.id} value={choice.id}>{choice.text}</Radio>)
            })
        }
        // const gridStyle = {
        //     // width: '25%',
        //     textAlign: 'center',
        // };
        return (
            <Col span={8}>
                <div className="poll-content">
                    <div className="poll-header">
                        <div className="poll-creator-info">
                            <Link className="creator-link" to={`/users/${this.props.poll.createdBy.username}`}>
                                {this.props.poll.createdBy.photo == null ?
                                    (<Avatar className="poll-creator-avatar"
                                        style={{ backgroundColor: getAvatarColor(this.props.poll.createdBy.name) }} >
                                        {this.props.poll.createdBy.name[0].toUpperCase()}
                                    </Avatar>) : (
                                        <Avatar className="poll-creator-avatar" src={"http://localhost:5000/api/file/getImage/" + this.props.poll.createdBy.photo} />)}
                                <span className="poll-creator-name">
                                    {this.props.poll.createdBy.name}
                                </span>
                                <span className="poll-creator-username">
                                    @{this.props.poll.createdBy.username}
                                </span>
                                <span className="poll-creation-date">
                                    {formatDateTime(this.props.poll.creationDateTime)}
                                </span>
                            </Link>
                        </div>
                    </div>
                    <Card hoverable title={this.props.poll.question}>
                        <div className="poll-choices">
                            {
                                <RadioGroup
                                    className="poll-choice-radio-group"
                                    onChange={this.props.handleVoteChange}
                                // value={this.props.currentVote}
                                // defaultValue={this.props.poll.selectedChoice}
                                >
                                    {pollChoices}
                                </RadioGroup>
                            }
                        </div>
                        {this.state.choiceCanAdd ? null : this.state.choiceAdd}
                        {
                            !this.props.poll.isAddChoice&&this.props.poll.createdBy.username!==this.props.currentUser.username||this.props.poll.expired?null:<Button type="dashed" onClick={this.addChoiceInput} disabled={!this.state.choiceCanAdd}>
                            <PlusOutlined /> Add a choice
                        </Button>
                        }
                        
                        {/* <Card.Grid style={gridStyle}>Content</Card.Grid> */}
                    </Card>
                    <div className="poll-footer">
                        {
                            !(/* this.props.poll.selectedChoice || */ this.props.poll.expired) ?
                                (<Button className="vote-button" disabled={!this.props.currentVote} onClick={this.props.handleVoteSubmit}>Vote</Button>) : null
                        }
                        <span className="total-votes">{this.props.poll.totalVotes} votes</span>
                        <span className="separator">•</span>
                        <span className="time-left">

                            {
                                this.props.poll.expired ? "Final results" :
                                <Countdown title="Countdown" value={this.getTimeCountDown(this.props.poll)} />
                            }
                            
                        </span>
                        
                    </div>
                </div>
            </Col>
            // <div className="poll-content">
            //     <div className="poll-header">
            //         <div className="poll-creator-info">
            //             <Link className="creator-link" to={`/users/${this.props.poll.createdBy.username}`}>
            //                 <Avatar className="poll-creator-avatar"
            //                     style={{ backgroundColor: getAvatarColor(this.props.poll.createdBy.name) }} >
            //                     {this.props.poll.createdBy.name[0].toUpperCase()}
            //                 </Avatar>
            //                 <span className="poll-creator-name">
            //                     {this.props.poll.createdBy.name}
            //                 </span>
            //                 <span className="poll-creator-username">
            //                     @{this.props.poll.createdBy.username}
            //                 </span>
            //                 <span className="poll-creation-date">
            //                     {formatDateTime(this.props.poll.creationDateTime)}
            //                 </span>
            //             </Link>
            //         </div>
            //         <div className="poll-question">
            //             {this.props.poll.question}
            //         </div>
            //     </div>
            //     <div className="poll-choices">
            //         {
            //             <RadioGroup
            //                 className="poll-choice-radio-group"
            //                 onChange={this.props.handleVoteChange}
            //                 //value={this.props.currentVote}
            //                 defaultValue={this.props.poll.selectedChoice}
            //             >
            //                 {pollChoices}
            //             </RadioGroup>
            //         }
            //     </div>
            //     <div className="poll-footer">
            //         {
            //             !(/* this.props.poll.selectedChoice || */ this.props.poll.expired) ?
            //                 (<Button className="vote-button" disabled={!this.props.currentVote} onClick={this.props.handleVoteSubmit}>Vote</Button>) : null
            //         }
            //         <span className="total-votes">{this.props.poll.totalVotes} votes</span>
            //         <span className="separator">•</span>
            //         <span className="time-left">
            //             {
            //                 this.props.poll.expired ? "Final results" :
            //                     this.getTimeRemaining(this.props.poll)
            //             }
            //         </span>
            //     </div>
            // </div>
        );
    }
}

function CompletedOrVotedPollChoice(props) {
    return (
        <div className="cv-poll-choice">
            <span className="cv-poll-choice-details">
                <span className="cv-choice-percentage">
                    {Math.round(props.percentVote * 100) / 100}%
                </span>
                <span className="cv-choice-text">
                    {props.choice.text}
                </span>
                {
                    props.isSelected ? (
                        <span className="selected-choice-icon">
                            <CheckCircleOutlined />
                        </span>
                    ) : null
                }
            </span>
            <span className={props.isWinner ? 'cv-choice-percent-chart winner' : 'cv-choice-percent-chart'}
                style={{ width: props.percentVote + '%' }}>
            </span>
        </div>
    );
}
function CompletedOrVotedCanVotePollChoice(props) {
    // const gridStyle = {
    //     // width: '25%',
    //     textAlign: 'center',
    // };
    return (

        // <div className="cv-poll-choice">
        //     <span className="cv-poll-choice-details">
        //         <span className="cv-choice-percentage">
        //             {Math.round(props.percentVote * 100) / 100}%
        //         </span>
        //         <span className="cv-choice-text">
        //             {props.choice.text}
        //         </span>
        //         {
        //             props.isSelected ? (
        //                 <Icon
        //                     className="selected-choice-icon"
        //                     type="check-circle-o"
        //                 />) : null
        //         }
        //     </span>
        //     <span className={props.isWinner ? 'cv-choice-percent-chart winner' : 'cv-choice-percent-chart'}
        //         style={{ width: props.percentVote + '%' }}>
        //     </span>
        // </div>

        <Radio className="poll-choice-radio" key={props.choice.id} value={props.choice.id}>
            <span className="cv-poll-choice-details2">
                
                <span className="cv-choice-percentage">
                    {Math.round(props.percentVote * 100) / 100}%
                </span>
                <span className="cv-choice-text">
                    {props.choice.text}
                </span>
                {/* <Card.Grid style={gridStyle}>{props.choice.text}</Card.Grid>
                {/* <span className="cv-choice-text">
                    {props.choice.text}
                </span> */}
            </span>
            <span className={props.isSelected?"cv-choice-percent-chart2 winner":"cv-choice-percent-chart2"}
                style={{ width: props.percentVote + '%' }}>
            </span>
            
            {
                props.isDelete ? (
                    <Button type="dashed" shape="circle" icon={<DeleteOutlined />} value={props.choice.id} onClick={props.handleClickDelChoice} size='small' className="delete-button-choice" />
                ) : null
            }
            {
                props.isSelected&&props.canFix ? (
                    <Button type="dashed" shape="circle" icon={<CloseOutlined />} value={props.choice.id} onClick={props.handleClick} size='small' className="delete-button" />
                ) : null
            }
        </Radio>

        // <div className="cv-poll-choice" >
        //     <Button onClick={this.props.handleVoteSubmit} onChange={this.props.handleVoteChange} 
        //                 value={this.props.currentVote}>
        //     <span className="cv-poll-choice-details">
        //         <span className="cv-choice-percentage">
        //             {Math.round(props.percentVote * 100) / 100}%
        //         </span>            
        //         <span className="cv-choice-text">
        //             {props.choice.text}
        //         </span>
        //         {
        //             props.isSelected ? (
        //             <Icon
        //                 className="selected-choice-icon"
        //                 type="check-circle-o"
        //             /> ): null
        //         }    
        //     </span>
        //     <span className={props.isWinner ? 'cv-choice-percent-chart winner': 'cv-choice-percent-chart'} 
        //         style={{width: props.percentVote + '%' }}>
        //     </span>
        //     </Button>
        // </div>
    );
}

export default Poll;