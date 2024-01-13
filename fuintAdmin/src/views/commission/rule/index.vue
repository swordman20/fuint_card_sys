<template>
  <div class="app-container">

    <!--查询参数-->
    <el-form size="small" :inline="true" label-width="68px">
      <el-form-item label="方案类别" prop="comType">
        <el-select v-model="comTypeParam" placeholder="请选择类别" clearable style="width: 100%;">
          <el-option key="1" label="项目" value="1"/>
          <el-option key="3" label="商品" value="3"/>
          <el-option key="4" label="次卡" value="4"/>
          <el-option key="5" label="储值" value="5"/>
        </el-select>
      </el-form-item>
      <el-form-item label="方案名称">
        <el-input
          v-model="comNameParam"
          placeholder="请输入方案名称"
          clearable
          style="width: 240px;"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          v-hasPermi="['commission:rule:index']"
          @click="handleAdd('')"
        >新增方案
        </el-button>
      </el-form-item>
    </el-form>

    <!--方案列表-->
    <el-table :data="tableData" border size="medium" style="width: 100%">
      <el-table-column prop="comName" label="方案名称"></el-table-column>
      <el-table-column prop="comType" label="商品分类" :formatter="dataFormat2"></el-table-column>
      <el-table-column label="创建时间">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            v-hasPermi="['scheme:add']"
            @click="handleAdd(scope.row.id)"
          >修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            v-hasPermi="['scheme:remove']"
            @click="handleDelete(scope.row)"
          >删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      style="margin-top: 5px;text-align: right"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      :page-sizes="[10, 20, 30, 40, 50, 60, 70, 80, 90, 100]"
      :page-size="pageSize"
      :current-page="currentPage"
      @current-change="getCommissionRuleList"
      @size-change="handleSizeChange"
    ></el-pagination>

    <!-- 添加或修改方案对话框 -->
    <el-dialog :title="title" :visible.sync="open" class="common-dialog" width="1100px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="126px">
        <el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="提成方案名称" prop="comName">
                <el-input v-model="form.comName" placeholder="请输入名称" maxlength="90" style="width: 55%"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-col :span="24">
            <el-form-item label="提成方案类别" prop="comType">
              <el-radio-group v-model="form.comType">
                <el-radio label="1">项目</el-radio>
                <el-radio label="3">商品</el-radio>
                <el-radio label="4">次卡</el-radio>
                <el-radio label="5">储值</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="方案适用门店" prop="storeIdList">
              <el-select v-model="form.storeIdList" filterable multiple clearable placeholder="请选择适用门店"
                         style="width: 55%">
                <el-option v-for="item in storeOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div style="margin-bottom: 20px;">
        <span style="size: 26px;font-weight: bold;margin-left: 20px;"><span
          style="color: red;margin-right: 5px;">*</span>提成规则配置：</span>
        <el-button type="primary" size="mini" icon="el-icon-plus" plain @click="addTc">添加项目提成</el-button>
      </div>

      <el-table :data="form.detailList" id="elTableIdWW" border size="medium" style="width: 100%">
        <el-table-column label="图片" align="center" width="136">
          <template slot-scope="scope">
            <img class="list-img" style="width: 45px;height: 35px;" :src="uploadAction + scope.row.logo">
          </template>
        </el-table-column>
        <el-table-column prop="goodsName" label="商品名称" min-width="136" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column label="标准价(原价)">
          <template slot-scope="scope">
            ￥ {{scope.row.price}} 元
          </template>
        </el-table-column>
        <el-table-column prop="comType" label="提成类型" :formatter="dataFormat"></el-table-column>

        <el-table-column label="价格类型" width="99">
            <template slot-scope="scope">
              原价
            </template>
        </el-table-column>
        <el-table-column prop="tcWay" label="提成方式" width="126">
          <template slot-scope="scope">
            <select :rowId="scope.row.goodsId" tp="tcWay" @change="selectChange(scope.row.goodsId,'tcWay')" style="border: 1px solid #dbdee5;">
              <option :selected="scope.row.tcWay=='1'?true:false" value="1">比率提成（%）</option>
              <option :selected="scope.row.tcWay=='2'?true:false" value="2">固定提成（元）</option>
            </select>
          </template>
        </el-table-column>
        <el-table-column prop="visitorVal" :label="form.comType=='5'?'首冲':'散客'" width="99">
          <template slot-scope="scope">
            <input :value="scope.row.visitorVal" v-if="form.comType != '4'" @change="inputChange(scope.row.goodsId,'visitorVal')" onkeyup="value=value.replace(/[^\d.]/g,'')" :rowId="scope.row.goodsId" tp="visitorVal"/>
          </template>
        </el-table-column>

        <el-table-column :label="form.comType=='5'?'次冲':'会员'" width="99">
          <template slot-scope="scope">
            <input :value="scope.row.memberVal" @change="inputChange(scope.row.goodsId,'memberVal')" onkeyup="value=value.replace(/[^\d.]/g,'')" :rowId="scope.row.goodsId" tp="memberVal"/>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="77">
          <template slot-scope="scope">
            <el-button size="mini" @click="removeGd(scope.row.goodsId)" slot="reference" type="text">删除<i class="el-icon-delete"></i>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open=false">取 消</el-button>
      </div>
    </el-dialog>

    <!--商品服务选择列表-->
    <el-dialog title="卡项/商品/服务选择" :visible.sync="openSelect" class="common-dialog" width="700px" append-to-body>
      <el-table
        :data="selectGoodList"
        border
        size="medium"
        ref="multipleTable"
        @selection-change="handleSelectionChange"
        :row-key="getRowKeys"
        style="width: 100%">
        <el-table-column
          type="selection"
          width="55"
          :reserve-selection="true"
        >
        </el-table-column>
        <el-table-column label="图片" align="center" width="156">
          <template slot-scope="scope">
            <img class="list-img" style="width: 45px;height: 35px;" :src="uploadAction + scope.row.logo">
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称"></el-table-column>
        <el-table-column label="标准价(原价)" width="100">
          <template slot-scope="scope">
            ￥{{scope.row.price}} 元
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="selectOk">确 定</el-button>
        <el-button @click="openSelect=false">取 消</el-button>
      </div>
    </el-dialog>


  </div>
</template>
<script>
  import { getCommissionRuleList, removeCommissionRule, getCommissionRuleInfo, saveCommissionRule, selectGoodList } from "@/api/commission/rule";
  import { searchStore } from "@/api/store";

  export default {
    name: 'busStaffList',
    data() {
      return {
        multipleSelection: [],
        uploadAction: process.env.VUE_APP_SERVER_URL + '/backendApi/file/getImg/',
        currentPage: 1, //初始页
        pageSize: 10, //每页的数据
        total: 0,
        tableData: [],
        selectGoodList: [],
        storeOptions: [],
        comNameParam: '',
        comTypeParam: '',
        title: '',
        open: false,
        openSelect: false,

        form: {
          id: '',
          storeIdList: [],
          comName: '',
          comType: '1',
          createTime: '',
          updateTime: '',
          status: '',
          detailList: []
        },
        rules: {
          storeIdList: [{required: true, message: '请选择方案使用门店', trigger: 'blur'}],
          comName: [{required: true, message: '请输入方案名称', trigger: 'blur'}],
          comType: [{required: true, message: '请选择商品分类', trigger: 'blur'}]
        }
      }
    },
    methods: {
      getCommissionRuleList(page) {
        let params = {
          page: page,
          limit: this.pageSize,
          comName: this.comNameParam,
          comType: this.comTypeParam
        }
        getCommissionRuleList(params).then(r => {
            this.tableData = r.data.rows;
            this.total = r.data.total;
          }
        );
      },
      handleSizeChange(val) {
        this.pageSize = val;
        this.getCommissionRuleList(1);
      },
      dataFormat: function (row, column) {
        //商品分类=提成类型：1：项目=劳动提成:2：卡项=销售提成:3：商品=销售提成
        let val = row[column.property];
        if (val == '1') {return '劳动提成'}
        if (val == '3') {return '销售提成'}
        if (val == '4') {return '销售提成'}
        if (val == '5') {return '销售提成'}
        return val;
      },
      dataFormat2: function (row, column) {
        //商品分类=提成类型：1：项目=劳动提成:2：卡项=销售提成:3：商品=销售提成
        let val = row[column.property];
        if (val == '1') {return '项目'}
        if (val == '3') {return '商品'}
        if (val == '4') {return '次卡'}
        if (val == '5') {return '储值'}
        return val;
      },
      // 店铺列表
      getStoreList() {
        searchStore().then(response => {
            this.storeOptions = response.data.storeList;
          }
        )
      },
      handleQuery() {
        this.getCommissionRuleList(1);
      },
      handleAdd(id) {
        this.title = "新增方案"
        this.form.id = '';
        this.form.storeIdList = [];
        this.form.detailList = [];
        this.form.comName = '';
        this.form.comType = '1';
        this.form.createTime = '';
        this.form.updateTime = '';
        this.form.status = '';
        this.open = true;
        if (id) {
          this.title = "修改方案"
          getCommissionRuleInfo({id: id}).then(r => {
              this.form = r.data;
            }
          );
        }
      },
      // 提交按钮
      submitForm: function () {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (!this.form.id) {
              saveCommissionRule(this.form).then(r => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.handleQuery();
              });
            } else {
              saveCommissionRule(this.form).then(r => {
                this.$modal.msgSuccess("新增成功");
                this.open = false;
                this.handleQuery();
              });
            }
          }
        });
      },
      handleDelete(row) {
        this.$confirm('确认删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let id = row.id;
          removeCommissionRule({id: id}).then(r => {
              if (r.code == 200) {
                this.$message({ // 成功
                  type: 'success',
                  message: '操作成功'
                })
                this.getCommissionRuleList(1);
              }
            }
          )
        }).catch(() => {
        });
      },
      addTc() {//添加提成方案详情
        if (!this.form.comType) {
          this.$modal.msgWarning("请先选择提成方案类别");
          return;
        }
        this.openSelect = true;

        this.multipleSelection = [];//清空已选择的列表
        //清空所有选中
        this.$nextTick(() => {
          this.$refs.multipleTable.clearSelection();
        })

        let params = {comType: this.form.comType}
        selectGoodList(params).then(r => {
            this.selectGoodList = r.data;
            //数据回显
            this.selectGoodList.forEach(item => {
              this.form.detailList.forEach(item2 => {
                if(item2.goodsId==item.id){
                  //满足条件选中
                  this.$refs.multipleTable.toggleRowSelection(item);
                }
              })
            })
          }
        );
      },
      removeGd(goodId){//删除提成方案商品服务
        this.form.detailList = this.form.detailList.filter(item => {
          return item.goodsId != goodId;
        })
      },
      inputChange(goodId,filed){
        let input = $("#elTableIdWW .el-table__body-wrapper tbody").find("input[rowId='" + goodId + "'][tp='" + filed + "']");
        if (input.val()=='') {
            input.val(0)
        }
        //去掉左右空格
        let val = Number($.trim(input.val()));
        input.val(val);
        this.form.detailList.forEach(item => {
          if (item.goodsId == goodId) {
            item[filed] = val;
          }
        })
      },
      selectChange(goodId,filed){
        let select = $("#elTableIdWW .el-table__body-wrapper tbody").find("select[rowId='" + goodId + "'][tp='" + filed + "']");
        let val = select.val();
        this.form.detailList.forEach(item => {
          if (item.goodsId == goodId) {
              item[filed] = val;
          }
        })
      },
      getRowKeys(row) {
        return row.id;
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      },
      selectOk() {//确定选择的商品服务
        let selectList = [];
        //1.转换已经选择的商品
        this.multipleSelection.forEach(item => {
          if (item.comType == this.form.comType) {
            let detailObj = {
              goodsId: item.id,//商品ID
              priceType: "1",//价格类型 1：标准价(原价) 2：会员价
              tcWay: "1",//提成方式 1：比率提成(按100的比率) 2：固定提成
              visitorVal: 0.00,//散客（提成值）
              memberVal: 0.00,//会员（提成值）
              comType: item.comType,//商品分类=提成类型：1：项目=劳动提成:3：商品=销售提成 4：次卡=销售提成: 5：储值=销售提成:
              goodsName: item.name,//商品名称
              price: item.price,//商品价格
              logo: item.logo//商品图片
            }

            //2.比对现有已选择的商品
            this.form.detailList.forEach(item2 => {
              if(item2.goodsId==item.id){
                detailObj = item2;
              }
            })

            selectList.push(detailObj);
          }
        })

        this.form.detailList = selectList;
        this.openSelect = false;
      }
    },
    created: function () {
      this.getCommissionRuleList(this.currentPage);
      this.getStoreList();
    }
  }
</script>
<style scoped>

  select {
    /*去除select边框*/
    border: 0;
    background: transparent;
    /*去除下拉框的三角下标*/
    appearance: none;
    -moz-appearance: none; /* Firefox */
    -webkit-appearance: none; /* Safari 和 Chrome */
    height: 32px;
    width: 100%;
    background-color: white;
    padding-left: 5px;
    border-radius: 5px;
  }

  input {
    width: 96%;
    height: 32px;
    font-size: 14px;
    color: #686868;
    border: 1px solid #d3d6dd;
    padding-left: 5px;
    border-radius: 5px;
  }

  .queryInput >>> .el-input__inner {
    border-radius: 0px;
    width: 200px;
  }
</style>
