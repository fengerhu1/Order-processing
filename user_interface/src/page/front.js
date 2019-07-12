import React, { Component } from 'react';
import {Input, Icon, Modal, Button, Table, Select, message} from 'antd'

const {Option} = Select

const columns = [
    {
        title: 'item id',
        dataIndex:'id',
        key: 0
    },
    {
        title: 'item number',
        dataIndex: 'number',
        key: 1
    }
]

const columnsOrder = [
    {
        title: 'user id',
        dataIndex: 'user_id',
        key: 1
    },
    {
        title: 'initiator',
        dataIndex: 'initiator',
        key: 2
    },
    {
        title: 'success',
        dataIndex: 'success',
        key: 3
    },
    {
        title: 'paid',
        dataIndex: 'paid',
        key: 4
    }
]

const IPaddress = 'http://202.120.40.8:30438/'

class Page extends Component {
    constructor(props){
        super(props);
        this.createOrder = this.createOrder.bind(this);
        //this.getUserid = this.getUserid.bind(this);
        this.state={
            user_id:'',
            order_items:[],
            initiator: 'RMB',
            showCreate: false,
            showSearch: false,
            itemid: '',
            itemnumber: '',
            orderid: '',
            searchResult:[]
        }
    }

    startCreateOrder = () => {
        this.setState({
            showCreate: true
        });
    }
    startSearchOrder = () => {
        this.setState({
            showSearch: true,
        });
    }
    createOrder = () => {
        if(!this.UserId(this.state.user_id))
        {
            return;
        }
        // const fd = new FormData();
        // fd.append('user_id', this.state.user_id)
        // fd.append('initiator', this.state.initiator)
        // fd.append('time', new Date().getTime())
        // fd.append('items', JSON.stringify(this.state.order_items))
        let jsonbody = {};
        jsonbody.use_id = Number.parseInt(this.state.user_id)
        jsonbody.initiator = this.state.initiator
        jsonbody.time = new Date().getTime()
        jsonbody.item = this.state.order_items
        let url = IPaddress + 'create_order';
        let options={};
        options.method='POST';
        options.headers={ 'Accept': 'application/json', 'content-type': 'application/json'};
        options.body = JSON.stringify(jsonbody)
        let that = this
        fetch(url, options)
            .then(response=>response.text())
            .then(responseJson=>{
                let mess = "下单成功！订单为：" + responseJson
                message.success(mess);
                that.setState({
                    user_id: '',
                    itemid: '',
                    itemnumber:'',
                    order_items:null
                })
            }).catch(function(e){
            console.log(e);
        });
    }

    cancelOrder = () => {
        this.setState({
            showCreate: false,
            user_id: '',
            itemid: '',
            itemnumber:'',
            order_items:null
        })
    }

    cancelSearch = () => {
        this.setState({
            showSearch: false,
            orderid:'',
            searchResult:null
        })
    }

    addItem = () => {
        if(!this.ItemId(this.state.itemid) || !this.ItemNumber(this.state.itemnumber))
        {
        }
        else
        {
            let id = this.state.itemid
            let number = Number.parseInt(this.state.itemnumber)
            let tmp = this.state.order_items
            tmp.push({id: id, number: number})
            this.setState({
                order_items: tmp
            })
        }
    }

    isInt(strings) {
        if(strings.length < 1)
        {
            return false;
        }
        for(let i = 0; i < strings.length; ++i)
        {
            if(strings[i] > '9' || strings[i] < '0')
            {
                return false;
            }
        }
        return true;
    }

    isInt16(strings) {
        if(strings.length < 1)
        {
            return false;
        }
        for(let i = 0; i < strings.length; ++i)
        {
            if((strings[i] > '9' || strings[i] < '0') && (strings[i] > 'f' || strings[i] < 'a'))
            {
                return false;
            }
        }
        return true;
    }

    modifyItemId = (id) => {
        this.setState({
            itemid: id.target.value
        })
    }

    modifyItemNumber = (number) => {
        this.setState({
            itemnumber: number.target.value
        })
    }

    modifyUserId = (id) => {
        this.setState({
            user_id: id.target.value
        })
    }

    modifyOrderId = (id) => {
        this.setState({
            orderid: id.target.value
        })
    }

    ItemId = (id) => {
        if(!this.isInt(id))
        {
            message.error("商品id需要为整数")
            this.setState({
                itemid: ''
            })
            return false;
        }
        let itemidn = Number.parseInt(id)
        if(itemidn < 1 || itemidn > 100)
        {
            message.error("仅可输入1~100的商品id")
            this.setState({
                itemid: ''
            })
            return false;
        }
        return true;
    }

    ItemNumber = (number) => {
        if (!this.isInt(number)) {
            message.error("商品数量需要为整数")
            this.setState({
                itemnumber: ''
            })
            return false;
        }
        let itemnumbern = Number.parseInt(number)
        if (itemnumbern < 1) {
            message.error("仅可输入正整数数量")
            this.setState({
                itemnumber: ''
            })
            return false;
        }
        return true;
    }

    UserId = (id) => {
        if (!this.isInt(id)) {
            message.error("用户id需要为整数")
            this.setState({
                user_id: ''
            })
            return false;
        }
        let itemnumbern = Number.parseInt(id)
        if (itemnumbern < 1) {
            message.error("仅可输入正整数id")
            this.setState({
                user_id: ''
            })
            return false;
        }
        return true;
    }

    OrderId = (id) => {
        if (!this.isInt16(id)) {
            message.error("订单id需要为整数")
            this.setState({
                orderid: ''
            })
            return false;
        }
        let itemnumbern = Number.parseInt(id)
        if (itemnumbern < 1) {
            message.error("仅可输入正整数id")
            this.setState({
                orderid: ''
            })
            return false;
        }
        return true;
    }

    searchOrder = () => {
        if(!this.OrderId(this.state.orderid))
        {

        }
        else
        {
            let url = IPaddress + 'get_order?order_id=' + this.state.orderid;
            let options={};
            options.method='GET';
            options.headers={ 'Accept': 'application/json', 'Content-Type': 'application/json'};
            let that = this
            fetch(url, options)
                .then(response=>response.text())
                .then(responseJson=>{
                    console.log(responseJson)
                    let result = eval(responseJson);
                    for(let i = 0; i < result.length; ++i){
                        if(result[i].success)
                        {
                            result[i].success = "true"
                        }
                        else{
                            result[i].success = "false"
                        }
                        result[i].paid = result[i].paid.toFixed(2)
                    }
                    that.setState({
                        searchResult: result
                    })
                }).catch(function(e){
                console.log(e);
            });
        }
    }

    render() {
        return (
            <div>
                <Button type="primary" onClick={this.startCreateOrder}>
                    创建订单
                </Button>
                <Button type="primary" onClick={this.startSearchOrder}>
                    查询订单
                </Button>
                <Modal
                    visible={this.state.showCreate}
                    title="创建订单"
                    onOK={this.createOrder}
                    onCancel={this.cancelOrder}
                    footer={[
                        <Button key="back" onClick={this.cancelOrder}>
                            取消
                        </Button>,
                        <Button key="submit" type="primary" onClick={this.createOrder}>
                            提交订单
                        </Button>,
                    ]}
                >
                    <Input
                        placeholder="Enter your user id"
                        prefix={<Icon type="user" style={{color: 'rgba(0,0,0,.25)'}}/>}
                        value={this.state.user_id}
                        onChange={this.modifyUserId}
                    />
                    <Select defaultValue="RMB" model={this.state.initiator}>
                        <Option value="RMB">RMB</Option>
                        <Option value="USD">USD</Option>
                        <Option value="JPY">JPY</Option>
                        <Option value="EUR">EUR</Option>
                    </Select>
                    <div>
                        <Input placeholder="Enter new item id" value={this.state.itemid} onChange={this.modifyItemId}/>
                    </div>
                    <div>
                        <Input placeholder="Enter new item number" value={this.state.itemnumber} onChange={this.modifyItemNumber}/>
                    </div>
                    <Button type="primary" icon="plus" onClick={this.addItem}/>

                    <Table
                        columns={columns}
                        dataSource={this.state.order_items}
                    />
                </Modal>
                <Modal
                    visible={this.state.showSearch}
                    title="查询订单"
                    onOk={this.cancelSearch}
                    onCancel={this.cancelSearch}
                    footer={[
                        <Button key="back" onClick={this.cancelSearch}>
                            返回
                        </Button>,
                        <Button key="submit" type="primary" onClick={this.cancelSearch}>
                            确认
                        </Button>,
                    ]}
                >
                    <Input
                        placeholder="Enter order id"
                        value={this.state.orderid}
                        onChange={this.modifyOrderId}
                    />
                    <Button type="primary" onClick={this.searchOrder}>查询</Button>
                    <Table
                        columns={columnsOrder}
                        dataSource={this.state.searchResult}
                    />
                </Modal>
            </div>
        );

    }
}
export default Page;