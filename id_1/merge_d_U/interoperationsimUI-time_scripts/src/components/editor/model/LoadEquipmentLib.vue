<template>

  <a-tabs v-model:active-key="loadMode">
    <a-tab-pane key="old" tab="选取已有文件">
      <a-upload :before-upload="beforeUploadEquipmentHandle" @remove="removeEquipmentFromFile">
        <a-button>
          <FileTextOutlined />
        </a-button>
      </a-upload>
    </a-tab-pane>

    <a-tab-pane key="new" tab="新建文件">
      <a-form
          :label-col="{ width: 8 }"
          :wrapper-col="{ span: 16 }"
          :model="equipment"
      >
        <a-form-item
            label="装备名称"
            name="name"
            :rules="[{ required: true, message: '请输入装备名称' }]"
        >
          <a-input v-model:value="equipment.name"/>
        </a-form-item>

        <a-form-item
            label="装备对应数据发送器软件"
            name="path"
            :rules="[{ required: true, message: '请指定该装备对应的数据发送器软件' }]"
        >
          <a-upload :before-upload="beforeUploadDataGeneratorHandle" :max-count="1">
            <a-button>
              <FileTextOutlined />
            </a-button>
          </a-upload>
        </a-form-item>

        <a-form-item
            label="装备资源子树描述"
            :rules="[{ required: true, validator: validateTree }]"
        >
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
              <a-textarea v-model:value="equipment.treeFile.data" placeholder="example" style="width: 100%"/>
              <a-space align="center" size="large">
                <a-button content="清空文本内容" danger @click="editTreeClearHandle">清空</a-button>
                <a-button content="保存文本内容并配置到装备" @click="editTreeSaveHandle">保存</a-button>
              </a-space>
            </a-space>
          </a-space>

        </a-form-item>
        <a-button content="保存装备文件并配置到装备库" @click.prevent="onSave">保存</a-button>
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

      //当前装备
      equipment: reactive({
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
      equipmentFiles: [],
    }
  },
  methods: {
    beforeUploadEquipmentHandle(file) {
      if (file.size === 0) {
        console.warn('file is empty')
        return false
      }
      file.text().then(res => {
        const fileData = JSON.parse(res.toString())
        this.equipmentFiles.push({
          path: file.path,
          data: fileData
        })
      })
      return false
    },
    loadEquipment() {
      //form files
      this.equipmentFiles.forEach((file) => {
        this.editInfoStore.loadEquipment({
          name: file.data["装备名称"],
          dir: file.data["装备软件目录"],
          path: file.data["装备数据发生器软件路径"],
          tree: {
            path: file.data["装备资源子树描述文件路径"],
            data: ''
          }
        })
      })
    },
    removeEquipmentFromFile(file) {
      console.log('removeEquipmentFromFile: ', file.originFileObj.path)
      this.equipmentFiles = this.equipmentFiles.filter(item => item.path !== file.originFileObj.path)
      console.log(this.equipmentFiles)
    },
    beforeUploadDataGeneratorHandle(file) {
      const [dir, path] = file.path.rsplit('/')
      this.equipment.dir = dir
      this.equipment.path = path
      return false
    },
    beforeUploadTreeHandle(file) {
      console.log('beforeUploadTreeHandle', file.path)
      this.equipment.treeFile.path = file.path
      return false
    },
    validateTree() {
      const valid = !!this.equipment.treeFile.path
      if (valid) {
        if (this.equipment.treeFile.path.endsWith('.json')) {
          return Promise.resolve()
        }
        return Promise.reject('请选择json格式的文件')
      } else {
        return Promise.reject('请指定该装备对应的资源子树描述文件')
      }
    },
    editTreeClearHandle() {
      this.equipment.treeFile.data = ''
    },
    editTreeSaveHandle() {
      if (this.equipment.name === '') {
        return false
      }
      this.equipment.treeFile.path = this.equipment.name + '_tree.json'
      console.log(this.equipment.treeFile.path)
      const blob = new Blob([this.equipment.treeFile.data], {type: "text/plain;charset=utf-8"});
      saveAs(blob, this.equipment.treeFile.path)
    },
    onSave() {
      console.log('onSave')
      if (this.equipment.name === '') {
        return false
      }
      const equipmentFilePath = this.equipment + '.json'
      console.log(this.equipment)
      this.equipmentFiles.push({
        path: equipmentFilePath,
        data: {
          '装备名称': this.equipment.name,
          '装备软件目录': this.equipment.dir,
          '装备数据发生器软件路径': this.equipment.path,
          '装备资源子树描述文件路径': this.equipment.treeFile.path,
        }
      })
    },
    onSubmit() {
      console.log('onSubmit')
      this.loadEquipment()
    }
  }
}


</script>

