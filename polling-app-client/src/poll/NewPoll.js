import React, { Component } from 'react';
import { createPoll } from '../util/APIUtils';
import { MAX_CHOICES, POLL_QUESTION_MAX_LENGTH, POLL_CHOICE_MAX_LENGTH } from '../constants';
import './NewPoll.css';
import { Form, Input, Button, Icon, Select, Col, notification, Switch, InputNumber ,Row} from 'antd';
import {
    CloseOutlined
} from '@ant-design/icons';
const Option = Select.Option;
const FormItem = Form.Item;
const { TextArea } = Input

class NewPoll extends Component {
    constructor(props) {
        super(props);
        this.state = {
            question: {
                text: ''
            },
            choices: [{
                text: ''
            }, {
                text: ''
            }],
            pollLength: {
                days: 1,
                hours: 0
            },
            isAnonymousUser: true,
            isAddChoice: false,
            isCanFix: true,
            maxVote: 1,
            maxVotePerChoice: 1,
            timeLoad: 0
        };
        this.addChoice = this.addChoice.bind(this);
        this.removeChoice = this.removeChoice.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleQuestionChange = this.handleQuestionChange.bind(this);
        this.handleChoiceChange = this.handleChoiceChange.bind(this);
        this.handlePollDaysChange = this.handlePollDaysChange.bind(this);
        this.handlePollHoursChange = this.handlePollHoursChange.bind(this);
        this.isFormInvalid = this.isFormInvalid.bind(this);
    }

    addChoice(event) {
        const choices = this.state.choices.slice();
        this.setState({
            choices: choices.concat([{
                text: ''
            }])
        });
    }

    removeChoice(choiceNumber) {
        const choices = this.state.choices.slice();
        this.setState({
            choices: [...choices.slice(0, choiceNumber), ...choices.slice(choiceNumber + 1)]
        });
    }

    handleSubmit() {
        const pollData = {
            question: this.state.question.text,
            choices: this.state.choices.map(choice => {
                return { text: choice.text }
            }),
            pollLength: this.state.pollLength,
            isAnonymousUser: this.state.isAnonymousUser,
            isAddChoice: this.state.isAddChoice,
            isCanFix: this.state.isCanFix,
            maxVote: this.state.maxVote,
            maxVotePerChoice: this.state.maxVotePerChoice,
            timeLoad: this.state.timeLoad
        };
        createPoll(pollData)
            .then(response => {
                this.props.history.push("/");
            }).catch(error => {
                if (error.status === 401) {
                    this.props.handleLogout('/login', 'error', 'You have been logged out. Please login create poll.');
                } else {
                    notification.error({
                        message: 'Polling App',
                        description: error.message || 'Sorry! Something went wrong. Please try again!'
                    });
                }
            });
    }

    validateQuestion = (questionText) => {
        if (questionText.length === 0) {
            return {
                validateStatus: 'error',
                errorMsg: 'Please enter your question!'
            }
        } else if (questionText.length > POLL_QUESTION_MAX_LENGTH) {
            return {
                validateStatus: 'error',
                errorMsg: `Question is too long (Maximum ${POLL_QUESTION_MAX_LENGTH} characters allowed)`
            }
        } else {
            return {
                validateStatus: 'success',
                errorMsg: null
            }
        }
    }

    handleQuestionChange(event) {
        const value = event.target.value;
        this.setState({
            question: {
                text: value,
                ...this.validateQuestion(value)
            }
        });
    }

    validateChoice = (choiceText) => {
        if (choiceText.length === 0) {
            return {
                validateStatus: 'error',
                errorMsg: 'Please enter a choice!'
            }
        } else if (choiceText.length > POLL_CHOICE_MAX_LENGTH) {
            return {
                validateStatus: 'error',
                errorMsg: `Choice is too long (Maximum ${POLL_CHOICE_MAX_LENGTH} characters allowed)`
            }
        } else {
            return {
                validateStatus: 'success',
                errorMsg: null
            }
        }
    }

    handleChoiceChange(event, index) {
        const choices = this.state.choices.slice();
        const value = event.target.value;

        choices[index] = {
            text: value,
            ...this.validateChoice(value)
        }

        this.setState({
            choices: choices
        });
    }


    handlePollDaysChange(value) {
        const pollLength = Object.assign(this.state.pollLength, { days: value });
        this.setState({
            pollLength: pollLength
        });
    }

    handlePollHoursChange(value) {
        const pollLength = Object.assign(this.state.pollLength, { hours: value });
        this.setState({
            pollLength: pollLength
        });
    }

    isFormInvalid() {
        if (this.state.question.validateStatus !== 'success') {
            return true;
        }

        for (let i = 0; i < this.state.choices.length; i++) {
            const choice = this.state.choices[i];
            if (choice.validateStatus !== 'success') {
                return true;
            }
        }
    }
    onChangeIsAnonymousUser = () => {
        this.setState({
            isAnonymousUser: !this.state.isAnonymousUser
        });
    }
    onChangeIsAddChoice = () => {
        this.setState({
            isAddChoice: !this.state.isAddChoice
        });
    }
    onChangeisCanFix = () => {
        this.setState({
            isCanFix: !this.state.isCanFix
        });
    }
    handleChangemaxVote = (value) => {
        this.setState({
            maxVote: value
        });
    }
    handleChangemaxVotePerChoice = (value) => {
        this.setState({
            maxVotePerChoice: value
        });
    }
    handleChangetimeLoad = (value) => {
        this.setState({
            timeLoad: value
        });
    }
    render() {
        const choiceViews = [];
        this.state.choices.forEach((choice, index) => {
            choiceViews.push(<PollChoice key={index} choice={choice} choiceNumber={index} removeChoice={this.removeChoice} handleChoiceChange={this.handleChoiceChange} />);
        });

        return (
            <div className="new-poll-container">
                <h1 className="page-title">Create Poll</h1>
                <div className="new-poll-content">
                    <Form onFinish={this.handleSubmit} className="create-poll-form">
                        <FormItem validateStatus={this.state.question.validateStatus}
                            help={this.state.question.errorMsg} className="poll-form-row">
                            <TextArea
                                placeholder="Enter your question"
                                style={{ fontSize: '16px' }}
                                autosize={{ minRows: 3, maxRows: 6 }}
                                name="question"
                                value={this.state.question.text}
                                onChange={this.handleQuestionChange} />
                        </FormItem>

                        {choiceViews}
                        <FormItem className="poll-form-row">
                            <Button type="dashed" onClick={this.addChoice} disabled={this.state.choices.length === MAX_CHOICES}>
                                <Icon type="plus" /> Add a choice
                            </Button>
                        </FormItem>
                        <FormItem className="poll-form-row">
                            <Col xs={24} sm={4}>
                                Poll length:
                            </Col>
                            <Col xs={24} sm={20}>
                                <span style={{ marginRight: '18px' }}>
                                    <Select
                                        name="days"
                                        defaultValue="1"
                                        onChange={this.handlePollDaysChange}
                                        value={this.state.pollLength.days}
                                        style={{ width: 60 }} >
                                        {
                                            Array.from(Array(8).keys()).map(i =>
                                                <Option key={i}>{i}</Option>
                                            )
                                        }
                                    </Select> &nbsp;Days
                                </span>
                                <span>
                                    <Select
                                        name="hours"
                                        defaultValue="0"
                                        onChange={this.handlePollHoursChange}
                                        value={this.state.pollLength.hours}
                                        style={{ width: 60 }} >
                                        {
                                            Array.from(Array(24).keys()).map(i =>
                                                <Option key={i}>{i}</Option>
                                            )
                                        }
                                    </Select> &nbsp;Hours
                                </span>
                            </Col>
                        </FormItem>
                        <Switch checkedChildren="Bầu chọn ẩn danh" unCheckedChildren="Không ẩn danh" defaultChecked onChange={this.onChangeIsAnonymousUser} />
                        <Switch checkedChildren="Cho phép thêm lựa chọn" unCheckedChildren="Cấm thêm lựa chọn" onChange={this.onChangeIsAddChoice} />
                        <Switch checkedChildren="Cho phép sửa lựa chọn" unCheckedChildren="Cấm sửa lựa chọn" defaultChecked onChange={this.onChangeisCanFix} />
                        <Row gutter={[32, 8]}>
                            <Col span={12}>Số lượng phiếu bầu tối đa:</Col>
                            <Col span={12}>Mỗi lựa chọn được bầu tối đa:</Col>

                            <Col span={12}><InputNumber min={1} defaultValue={1} value={this.state.maxVote} onChange={this.handleChangemaxVote} />
                                    &nbsp;Lần</Col>
                            <Col span={12}><InputNumber min={1} defaultValue={1} value={this.state.maxVotePerChoice} onChange={this.handleChangemaxVotePerChoice} />
                        &nbsp;Lần</Col>
                        </Row>
                                    <Col xs={24} sm={10}>
                            Thời gian được thiết lập lại:
                            </Col>
                        <Select
                            name="timeRS"
                            defaultValue="0"
                            onChange={this.handleChangetimeLoad}
                            value={this.state.timeLoad}
                            style={{ width: 60 }} >
                            {
                                Array.from(Array(100).keys()).map(i =>
                                    <Option key={i}>{i}</Option>
                                )
                            }
                        </Select>
                        <FormItem className="poll-form-row">
                            <Button type="primary"
                                htmlType="submit"
                                size="large"
                                disabled={this.isFormInvalid()}
                                className="create-poll-form-button">Create Poll</Button>
                        </FormItem>
                    </Form>
                </div>
            </div>
        );
    }
}

function PollChoice(props) {
    return (
        <FormItem validateStatus={props.choice.validateStatus}
            help={props.choice.errorMsg} className="poll-form-row">
            <Input
                placeholder={'Choice ' + (props.choiceNumber + 1)}
                size="large"
                value={props.choice.text}
                className={props.choiceNumber > 1 ? "optional-choice" : null}
                onChange={(event) => props.handleChoiceChange(event, props.choiceNumber)} />

            {
                props.choiceNumber > 1 ? (
                    <CloseOutlined className="dynamic-delete-button" disabled={props.choiceNumber <= 1} onClick={() => props.removeChoice(props.choiceNumber)}/>
                    // <Icon
                    //     className="dynamic-delete-button"
                    //     type="close"
                    //     disabled={props.choiceNumber <= 1}
                    //     onClick={() => props.removeChoice(props.choiceNumber)}
                    // />
                    ) : null
            }
        </FormItem>
    );
}


export default NewPoll;