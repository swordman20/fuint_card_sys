<template>
  <div class="app-container">
    <div class="main-panel">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="用户名" prop="accountName">
              <el-input v-model="form.accountName" placeholder="请输入用户名" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item v-if="form.id == undefined" label="登录密码" prop="password">
              <el-input v-model="form.password" placeholder="请输入登录密码" maxlength="30" show-password/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item v-if="form.id == undefined" label="确认密码" prop="password1">
              <el-input v-model="form.password1" placeholder="请输入确认密码" type="password" maxlength="30" show-password/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="状态">
              <el-radio-group v-model="form.accountStatus">
                <el-radio :key="1" :label="1" :value="1">启用</el-radio>
                <el-radio :key="0" :label="0" :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="分配角色">
              <el-select v-model="form.roleIds" multiple placeholder="请选择角色">
                <el-option
                  v-for="item in roleOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                  :disabled="item.status !== 'A'"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="管辖店铺">
              <el-select v-model="form.storeId" placeholder="请选择管辖店铺">
                <el-option
                  v-for="item in storeOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                  :disabled="item.status !== 'A'"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { getAccountList, getAccount, delAccount, addAccount, updateAccount, resetAccountPwd, changeAccountStatus } from "@/api/system/account";
import { getToken } from "@/utils/auth";

export default {
  name: "StoreAdd",
  data() {
    return {
      // 遮罩层
      loading: false,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 用户表格数据
      userList: null,
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: true,
      // 角色选项
      roleOptions: [],
      storeOptions: [],
      // 表单参数
      form: { accountName: "", realName: "", accountStatus: "1", roleIds: "0", storeId: "0" },
      defaultProps: {
        children: "children",
        label: "label"
      },
      // 用户导入参数
      upload: {
        // 是否显示弹出层（用户导入）
        open: false,
        // 弹出层标题（用户导入）
        title: "",
        // 是否禁用上传
        isUploading: false,
        // 是否更新已经存在的用户数据
        updateSupport: 0,
        // 设置上传的请求头部
        headers: { 'Access-Token': getToken() },
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/system/account/importData"
      },
      // 查询参数
      queryParams: {
        page: 1,
        pageSize: 10,
        accountName: "",
        accountStatus: "",
      },
      // 列信息
      columns: [
        { key: 0, label: `用户编号`, visible: true },
        { key: 1, label: `用户名称`, visible: true },
        { key: 2, label: `用户昵称`, visible: true },
        { key: 3, label: `状态`, visible: true },
        { key: 4, label: `创建时间`, visible: true }
      ],
      // 表单校验
      rules: {
        accountName: [
          { required: true, message: "用户名不能为空", trigger: "blur" },
          { min: 2, max: 30, message: '用户名长度必须介于 2 和 20 之间', trigger: 'blur' }
        ],
        password: [
          { required: true, message: "用户密码不能为空", trigger: "blur" },
          { min: 5, max: 30, message: '用户密码长度必须介于 5 和 20 之间', trigger: 'blur' }
        ],
        password1: [
          { required: true, message: "确认密码不能为空", trigger: "blur" },
          { min: 5, max: 30, message: '确认密码长度必须介于 5 和 20 之间', trigger: 'blur' }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 查询账户列表
    getList() {
      this.loading = true;
      getAccountList(this.queryParams).then(response => {
          this.userList = response.data.content;
          this.total = response.data.totalElements;
          this.loading = false;
        }
      );
    },
    // 用户状态修改
    handleStatusChange(row) {
      let text = row.accountStatus == "1" ? "启用" : "禁用";
      this.$modal.confirm('确认要' + text + '"' + row.accountName + '"用户吗？').then(function() {
        return changeAccountStatus(row.id, row.accountStatus);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
        row.accountStatus = row.accountStatus === "0" ? "1" : "0";
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: undefined,
        accountName: "",
        password: "",
        accountStatus: "1",
        realName: "",
        roleIds: []
      };
      this.resetForm("form");
    },
    // 搜索按钮操作
    handleQuery() {
      this.queryParams.page = 1;
      this.getList();
    },
    // 重置按钮操作
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    // 更多操作触发
    handleCommand(command, row) {
      switch (command) {
        case "handleResetPwd":
          this.handleResetPwd(row);
          break;
        case "handleAuthRole":
          this.handleAuthRole(row);
          break;
        default:
          break;
      }
    },
    // 新增按钮操作
    handleAdd() {
      this.reset();
      getAccount(-1).then(response => {
        this.roleOptions = response.data.roles;
        this.storeOptions = response.data.stores;
        this.open = true;
        this.title = "新增管理员";
      });
    },
    // 修改按钮操作
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getAccount(id).then(response => {
        this.form = response.data.account;

        console.log('form = ', this.form)

        this.form.roleIds = response.data.roleIds;
        this.roleOptions = response.data.roles;
        this.storeOptions = response.data.stores;
        this.open = true;
        this.title = "修改用户";
        this.form.password = "";
      });
    },
    // 重置密码按钮操作
    handleResetPwd(row) {
      this.$prompt('请输入"' + row.accountName + '"的新密码', "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        closeOnClickModal: false,
        inputPattern: /^.{5,20}$/,
        inputErrorMessage: "用户密码长度必须介于 5 和 20 之间"
      }).then(({ value }) => {
          resetAccountPwd(row.id, value).then(response => {
            this.$modal.msgSuccess("修改成功，新密码是：" + value);
          });
        }).catch(() => {});
    },
    // 分配角色操作
    handleAuthRole: function(row) {
      const id = row.id;
      this.$router.push("/system/user-auth/role/" + id);
    },
    // 提交按钮
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != undefined) {
            updateAccount(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addAccount(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    // 删除按钮操作
    handleDelete(row) {
      const userIds = row.id || this.ids;
      this.$modal.confirm('您确认删除用户ID为"' + userIds + '"的账户？').then(function() {
        return delAccount(userIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>
