<template>

  <a-tabs v-model:active-key="loadMode">
    <a-tab-pane key="old" tab="选取已有文件">
      <a-upload :before-upload="beforeUploadApplicationHandle" @remove="removeApplicationFromFile">
        <a-button>
          <FileTextOutlined />
        </a-button>
      </a-upload>
    </a-tab-pane>

    <a-tab-pane key="new" tab="新建文件">
      <a-form
          :label-col="{ width: 8 }"
          :wrapper-col="{ span: 16 }"
          :model="application"
      >
        <a-form-item
            label="应用名称"
            name="name"
            :rules="[{ required: true, message: '请输入应用名称' }]"
        >
          <a-input v-model:value="application.name"/>
        </a-form-item>

        <a-form-item
            label="应用软件"
            name="path"
            :rules="[{ required: true, message: '请指定对应的应用软件' }]"
        >
          <a-upload :before-upload="beforeUploadSoftwareHandle" :max-count="1">
            <a-button>
              <FileTextOutlined />
            </a-button>
          </a-upload>
        </a-form-item>

        <a-form-item
            label="应用资源子树描述"
            :rules="[{ required: true, validator: validateTree }]"
        >
          <!--          :rules="[{ required: true, validator: validateTree }]"-->
          <a-form-item-rest>
            <a-radio-group v-model:value="treeLoadMode" style="width: 100%">
              <a-radio-button value="old">
                选取已有文件
              </a-radio-button>
              <a-radio-button value="new">
                新建文件
              </a-radio-button>
            </a-radio-group>
          </a-form-item-rest>

          <a-space style="width: 100%">
            <a-upload v-if="treeLoadMode === 'old'" :before-upload="beforeUploadTreeHandle" :max-count="1">
              <a-button>
                <FileTextOutlined />
              </a-button>
            </a-upload>
            <a-space v-if="treeLoadMode === 'new'" direction="vertical">
              <a-textarea v-model:value="application.treeFile.data" placeholder="example" style="width: 100%"/>
              <a-space direction="horizontal">
                <a-button content="清空文本内容" danger @click="editTreeClearHandle">清空</a-button>
                <a-button content="保存文本内容并配置到装备" @click="editTreeSaveHandle">保存</a-button>
              </a-space>
            </a-space>
          </a-space>

        </a-form-item>
        <a-button content="保存装备文件并配置到应用库" @click.prevent="onSave">保存</a-button>
      </a-form>
    </a-tab-pane>
  </a-tabs>

  <a-divider style="height: 2px; background-color: #05b390" dashed/>

  <a-button type="primary" html-type="submit" @click="onSubmit">提交</a-button>


</template>

<script>
import {useEditInfoStore} from "@/stores/EditInfoStore";
import {FileTextOutlined} from "@ant-design/icons-vue"
import {saveAs} from "file-saver";
import {reactive} from "vue";

String.prototype.rsplit = function (sep, maxsplit = 1) {
  const split = this.split(sep)
  return maxsplit ? [split.slice(0, -maxsplit).join(sep)].concat(split.slice(-maxsplit)) : split
}

export default {
  components: {FileTextOutlined},
  data() {
    return {
      editInfoStore: useEditInfoStore(),

      //当前应用
      application: reactive({
        name: '',
        dir: '',
        path: '',
        treeFile: {
          path: '',
          data: ''
        }
      }),

      loadMode: 'old',
      treeLoadMode: 'old',

      //临时
      applicationFiles: [],
    }
  },
  methods: {
    beforeUploadApplicationHandle(file) {
      if (file.size === 0) {
        console.warn('file is empty')
        return false
      }
      file.text().then(res => {
        const fileData = JSON.parse(res.toString())
        this.applicationFiles.push({
          path: file.path,
          data: fileData
        })
      })
      return false
    },
    loadApplication() {
      //form files
      this.applicationFiles.forEach((file) => {
        this.editInfoStore.loadApplication({
          name: file.data["应用名称"],
          dir: file.data["应用软件目录"],
          path: file.data["软件路径"],
          tree: {
            path: file.data["应用资源子树描述文件路径"],
            data: ''
          }
        })
      })
    },
    removeApplicationFromFile(file) {
      this.applicationFiles = this.applicationFiles.filter(item => item.path !== file.originFileObj.path)
    },
    beforeUploadSoftwareHandle(file) {
      const [dir, path] = file.path.rsplit('/')
      this.application.dir = dir
      this.application.path = path
      return false
    },
    beforeUploadTreeHandle(file) {
      this.application.treeFile.path = file.path
      return false
    },
    validateTree() {
      const valid = !!this.application.treeFile.path
      if (valid) {
        return Promise.resolve()
      } else {
        return Promise.reject('请指定该应用对应的资源子树描述文件')
      }
    },
    editTreeClearHandle() {
      this.application.treeFile.data = ''
    },
    editTreeSaveHandle() {
      if (this.application.name === '') {
        return false
      }
      this.application.treeFile.path = this.application.name + '_tree.json'
      console.log(this.application.treeFile.path)
      const blob = new Blob([this.application.treeFile.data], {type: "text/plain;charset=utf-8"});
      saveAs(blob, this.application.treeFile.path)
    },
    onSave() {
      console.log('onSave')
      if (this.application.name === '') {
        return false
      }
      const applicationFilePath = this.application + '.json'
      this.applicationFiles.push({
        path: applicationFilePath,
        data: {
          '应用名称': this.application.name,
          '应用软件目录': this.application.dir,
          '软件路径': this.application.path,
          '应用资源子树描述文件路径': this.application.treeFile.path,
        }
      })
    },
    onSubmit() {
      console.log('onSubmit')
      this.loadApplication()
      console.log(this.editInfoStore.$state)
    }
  }
}


</script>

